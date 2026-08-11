using Acme.Payments.CardPaymentService.Domain;
using Acme.Payments.CardPaymentService.Infrastructure;
using Acme.Payments.CardPaymentService.Providers;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.Diagnostics.Metrics;
using Microsoft.Extensions.Logging.Abstractions;

namespace Acme.Payments.CardPaymentService.Application;

public sealed class ApplicationCardPaymentService
{
    private static readonly Meter Meter = new("Acme.Payments.CardPaymentService");
    private static readonly Counter<long> Completed = Meter.CreateCounter<long>("payments.completed");
    private static readonly Counter<long> Conflicts = Meter.CreateCounter<long>("payments.idempotency.conflicts");
    private static readonly Histogram<double> Duration = Meter.CreateHistogram<double>("payments.authorization.duration", "ms");

    private readonly IReadOnlyCollection<ICardProviderAdapter> adapters;
    private readonly IIdempotencyStore idempotencyStore;
    private readonly ILogger<ApplicationCardPaymentService> logger;
    private readonly ConcurrentDictionary<string, SemaphoreSlim> locks = new();

    public ApplicationCardPaymentService(
        IEnumerable<ICardProviderAdapter> adapters,
        IIdempotencyStore idempotencyStore,
        ILogger<ApplicationCardPaymentService> logger)
    {
        this.adapters = adapters.ToArray();
        this.idempotencyStore = idempotencyStore;
        this.logger = logger;
    }

    public ApplicationCardPaymentService(IEnumerable<ICardProviderAdapter> adapters, IIdempotencyStore idempotencyStore)
        : this(adapters, idempotencyStore, NullLogger<ApplicationCardPaymentService>.Instance)
    {
    }

    public async Task<CardPaymentResult> AuthorizeAsync(CardPaymentCommand command, CancellationToken cancellationToken)
    {
        Validate(command);
        string fingerprint = RequestFingerprint.Of(command);
        SemaphoreSlim gate = locks.GetOrAdd(command.IdempotencyKey, _ => new SemaphoreSlim(1, 1));
        var startedAt = Stopwatch.GetTimestamp();
        await gate.WaitAsync(cancellationToken);

        try
        {
            CardPaymentResult? existing = await idempotencyStore.FindAsync(command.IdempotencyKey, cancellationToken);
            if (existing is not null)
            {
                await EnsureSameRequestAsync(command, fingerprint, cancellationToken);
                logger.LogInformation("payment_outcome service=card-payment operation=authorization outcome=reused payment_id={PaymentId} idempotency_key_hash={IdempotencyKeyHash} provider={Provider} status={Status}", existing.PaymentId, fingerprint[..12], existing.Provider, existing.Status);
                return existing;
            }

            ICardProviderAdapter adapter = ChooseProvider(command);
            var reservation = new CardPaymentResult(Guid.NewGuid(), adapter.Provider, CardPaymentStatus.PendingReview, $"pending:{command.IdempotencyKey}");
            if (!await idempotencyStore.TryReserveAsync(command.IdempotencyKey, command, reservation, fingerprint, cancellationToken))
            {
                await EnsureSameRequestAsync(command, fingerprint, cancellationToken);
                return await idempotencyStore.FindAsync(command.IdempotencyKey, cancellationToken)
                       ?? throw new InvalidOperationException("idempotency reservation has no result");
            }

            CardPaymentResult result = await adapter.AuthorizeAsync(command, cancellationToken);
            await idempotencyStore.SaveAsync(command.IdempotencyKey, command, result, fingerprint, cancellationToken);
            Completed.Add(1, new KeyValuePair<string, object?>("provider", result.Provider.ToString()));
            logger.LogInformation("payment_outcome service=card-payment operation=authorization outcome=completed payment_id={PaymentId} idempotency_key_hash={IdempotencyKeyHash} provider={Provider} status={Status}", result.PaymentId, fingerprint[..12], result.Provider, result.Status);
            return result;
        }
        catch (IdempotencyConflictException)
        {
            Conflicts.Add(1);
            logger.LogWarning("payment_outcome service=card-payment operation=authorization outcome=idempotency_conflict idempotency_key_hash={IdempotencyKeyHash}", fingerprint[..12]);
            throw;
        }
        finally
        {
            Duration.Record(Stopwatch.GetElapsedTime(startedAt).TotalMilliseconds);
            gate.Release();
        }
    }

    private async Task EnsureSameRequestAsync(CardPaymentCommand command, string fingerprint, CancellationToken cancellationToken)
    {
        string? stored = await idempotencyStore.FindFingerprintAsync(command.IdempotencyKey, cancellationToken);
        if (stored is not null && !string.Equals(stored, fingerprint, StringComparison.Ordinal))
        {
            throw new IdempotencyConflictException(command.IdempotencyKey);
        }
    }

    private ICardProviderAdapter ChooseProvider(CardPaymentCommand command)
    {
        if (command.PreferredProvider is not null)
        {
            return adapters.FirstOrDefault(adapter =>
                       adapter.Provider == command.PreferredProvider && adapter.Supports(command))
                   ?? throw new InvalidOperationException($"{command.PreferredProvider} is unavailable for this card payment");
        }

        return adapters.FirstOrDefault(adapter => adapter.Provider == PaymentProvider.Stripe && adapter.Supports(command))
               ?? adapters.First(adapter => adapter.Supports(command));
    }

    private static void Validate(CardPaymentCommand command)
    {
        if (string.IsNullOrWhiteSpace(command.IdempotencyKey)) throw new ArgumentException("idempotency key is required", nameof(command));
        if (command.Amount <= 0) throw new ArgumentException("amount must be greater than zero", nameof(command));
        if (string.IsNullOrWhiteSpace(command.Currency) || command.Currency.Length != 3) throw new ArgumentException("currency must use ISO-4217 alpha-3 format", nameof(command));
        if (string.IsNullOrWhiteSpace(command.CardToken)) throw new ArgumentException("card token is required", nameof(command));
    }
}
