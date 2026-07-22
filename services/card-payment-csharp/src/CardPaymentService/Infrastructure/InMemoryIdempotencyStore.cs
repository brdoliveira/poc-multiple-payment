using Acme.Payments.CardPaymentService.Domain;
using System.Collections.Concurrent;

namespace Acme.Payments.CardPaymentService.Infrastructure;

public sealed class InMemoryIdempotencyStore : IIdempotencyStore
{
    private readonly ConcurrentDictionary<string, CardPaymentResult> entries = new();

    public Task<CardPaymentResult?> FindAsync(string key, CancellationToken cancellationToken)
    {
        entries.TryGetValue(key, out CardPaymentResult? result);
        return Task.FromResult(result);
    }

    public Task SaveAsync(string key, CardPaymentCommand command, CardPaymentResult result, CancellationToken cancellationToken)
    {
        entries.TryAdd(key, result);
        return Task.CompletedTask;
    }
}
