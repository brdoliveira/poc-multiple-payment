using System.Text.Json;

namespace Acme.Payments.CardPaymentService.Messaging;

public sealed record PaymentProcessingEvent
{
    public string EventId { get; init; } = string.Empty;

    public string EventType { get; init; } = string.Empty;

    public string PaymentId { get; init; } = string.Empty;

    public string IdempotencyKey { get; init; } = string.Empty;

    public string Method { get; init; } = string.Empty;

    public decimal Amount { get; init; }

    public string Currency { get; init; } = string.Empty;

    public string? Provider { get; init; }

    public IReadOnlyDictionary<string, JsonElement> Metadata { get; init; } =
        new Dictionary<string, JsonElement>();
}
