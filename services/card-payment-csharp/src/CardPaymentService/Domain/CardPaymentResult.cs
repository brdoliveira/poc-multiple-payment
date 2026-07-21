namespace Acme.Payments.CardPaymentService.Domain;

public sealed record CardPaymentResult(
    Guid PaymentId,
    PaymentProvider Provider,
    CardPaymentStatus Status,
    string ExternalAuthorizationId
);
