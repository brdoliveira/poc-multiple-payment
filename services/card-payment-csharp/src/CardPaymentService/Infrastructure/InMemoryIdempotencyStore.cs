using Acme.Payments.CardPaymentService.Domain;
using System.Collections.Concurrent;

namespace Acme.Payments.CardPaymentService.Infrastructure;

public sealed class InMemoryIdempotencyStore : IIdempotencyStore
{
    private sealed record Entry(CardPaymentResult Result, string Fingerprint);

    private readonly ConcurrentDictionary<string, Entry> entries = new();

    public Task<CardPaymentResult?> FindAsync(string key, CancellationToken cancellationToken)
    {
        entries.TryGetValue(key, out Entry? entry);
        return Task.FromResult(entry?.Result);
    }

    public Task<string?> FindFingerprintAsync(string key, CancellationToken cancellationToken)
    {
        entries.TryGetValue(key, out Entry? entry);
        return Task.FromResult(entry?.Fingerprint);
    }

    public Task<bool> TryReserveAsync(
        string key,
        CardPaymentCommand command,
        CardPaymentResult reservation,
        string requestFingerprint,
        CancellationToken cancellationToken)
    {
        return Task.FromResult(entries.TryAdd(key, new Entry(reservation, requestFingerprint)));
    }

    public Task SaveAsync(string key, CardPaymentCommand command, CardPaymentResult result, string requestFingerprint, CancellationToken cancellationToken)
    {
        entries[key] = new Entry(result, requestFingerprint);
        return Task.CompletedTask;
    }
}
