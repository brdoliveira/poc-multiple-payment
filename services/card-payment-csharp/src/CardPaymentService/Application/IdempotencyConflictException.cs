namespace Acme.Payments.CardPaymentService.Application;

public sealed class IdempotencyConflictException : InvalidOperationException
{
    public IdempotencyConflictException(string key)
        : base($"idempotency key was already used with a different request: {key}")
    {
    }
}
