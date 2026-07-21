using Acme.Payments.CardPaymentService.Domain;
using Acme.Payments.CardPaymentService.Infrastructure;
using Acme.Payments.CardPaymentService.Providers;

namespace Acme.Payments.CardPaymentService.Application;

public sealed class ApplicationCardPaymentService
{
    private readonly IReadOnlyCollection<ICardProviderAdapter> adapters;
    private readonly IIdempotencyStore idempotencyStore;

    public ApplicationCardPaymentService(IEnumerable<ICardProviderAdapter> adapters, IIdempotencyStore idempotencyStore)
    {
        this.adapters = adapters.ToArray();
        this.idempotencyStore = idempotencyStore;
    }

    public async Task<CardPaymentResult> AuthorizeAsync(CardPaymentCommand command, CancellationToken cancellationToken)
    {
        Validate(command);

        CardPaymentResult? existing = await idempotencyStore.FindAsync(command.IdempotencyKey, cancellationToken);
        if (existing is not null)
        {
            return existing;
        }

        ICardProviderAdapter adapter = ChooseProvider(command);
        CardPaymentResult result = await adapter.AuthorizeAsync(command, cancellationToken);
        await idempotencyStore.SaveAsync(command.IdempotencyKey, result, cancellationToken);
        return result;
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
        if (string.IsNullOrWhiteSpace(command.IdempotencyKey))
        {
            throw new ArgumentException("idempotency key is required", nameof(command));
        }

        if (command.Amount <= 0)
        {
            throw new ArgumentException("amount must be greater than zero", nameof(command));
        }

        if (command.Currency.Length != 3)
        {
            throw new ArgumentException("currency must use ISO-4217 alpha-3 format", nameof(command));
        }

        if (string.IsNullOrWhiteSpace(command.CardToken))
        {
            throw new ArgumentException("card token is required", nameof(command));
        }
    }
}
