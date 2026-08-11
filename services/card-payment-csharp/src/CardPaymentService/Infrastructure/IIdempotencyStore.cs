using Acme.Payments.CardPaymentService.Domain;

namespace Acme.Payments.CardPaymentService.Infrastructure;

public interface IIdempotencyStore
{
    Task<CardPaymentResult?> FindAsync(string key, CancellationToken cancellationToken);

    Task<string?> FindFingerprintAsync(string key, CancellationToken cancellationToken);

    Task<bool> TryReserveAsync(
        string key,
        CardPaymentCommand command,
        CardPaymentResult reservation,
        string requestFingerprint,
        CancellationToken cancellationToken);

    Task SaveAsync(string key, CardPaymentCommand command, CardPaymentResult result, string requestFingerprint, CancellationToken cancellationToken);
}
