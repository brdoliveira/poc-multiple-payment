using Acme.Payments.CardPaymentService.Domain;

namespace Acme.Payments.CardPaymentService.Infrastructure;

public interface IIdempotencyStore
{
    Task<CardPaymentResult?> FindAsync(string key, CancellationToken cancellationToken);

    Task SaveAsync(string key, CardPaymentCommand command, CardPaymentResult result, CancellationToken cancellationToken);
}
