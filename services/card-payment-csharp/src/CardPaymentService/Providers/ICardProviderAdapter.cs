using Acme.Payments.CardPaymentService.Domain;

namespace Acme.Payments.CardPaymentService.Providers;

public interface ICardProviderAdapter
{
    PaymentProvider Provider { get; }

    bool Supports(CardPaymentCommand command);

    Task<CardPaymentResult> AuthorizeAsync(CardPaymentCommand command, CancellationToken cancellationToken);
}
