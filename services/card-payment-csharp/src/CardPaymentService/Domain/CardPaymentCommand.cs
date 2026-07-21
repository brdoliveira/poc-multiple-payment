namespace Acme.Payments.CardPaymentService.Domain;

public sealed record CardPaymentCommand(
    string IdempotencyKey,
    decimal Amount,
    string Currency,
    int Installments,
    string CardToken,
    PaymentProvider? PreferredProvider
);
