namespace Acme.Payments.CardPaymentService.Domain;

public enum CardPaymentStatus
{
    Authorized,
    Declined,
    PendingReview
}
