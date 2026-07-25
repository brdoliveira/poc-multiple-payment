using System.Text.Json;
using Acme.Payments.CardPaymentService.Domain;

namespace Acme.Payments.CardPaymentService.Messaging;

public sealed class PaymentProcessingCommandMapper
{
    public CardPaymentCommand? ToCommand(PaymentProcessingEvent paymentEvent)
    {
        if (!string.Equals(paymentEvent.EventType, RabbitPaymentTopology.PaymentProcessingRoutingKey, StringComparison.Ordinal) ||
            !string.Equals(paymentEvent.Method, "CREDIT_CARD", StringComparison.Ordinal))
        {
            return null;
        }

        return new CardPaymentCommand(
            paymentEvent.IdempotencyKey,
            paymentEvent.Amount,
            paymentEvent.Currency.ToUpperInvariant(),
            MetadataInt(paymentEvent, "installments") ?? 1,
            MetadataString(paymentEvent, "cardToken") ?? $"event-token:{paymentEvent.PaymentId}",
            Provider(paymentEvent)
        );
    }

    private static PaymentProvider? Provider(PaymentProcessingEvent paymentEvent)
    {
        string? provider = paymentEvent.Provider ?? MetadataString(paymentEvent, "preferredProvider");
        return provider?.ToUpperInvariant() switch
        {
            "ASAAS" => PaymentProvider.Asaas,
            "MERCADO_PAGO" => PaymentProvider.MercadoPago,
            "PAGBANK" => PaymentProvider.PagBank,
            "IUGU" => PaymentProvider.Iugu,
            "STRIPE" => PaymentProvider.Stripe,
            _ => null
        };
    }

    private static string? MetadataString(PaymentProcessingEvent paymentEvent, string key)
    {
        if (!paymentEvent.Metadata.TryGetValue(key, out JsonElement value))
        {
            return null;
        }

        return value.ValueKind switch
        {
            JsonValueKind.String => value.GetString(),
            JsonValueKind.Number => value.GetRawText(),
            _ => null
        };
    }

    private static int? MetadataInt(PaymentProcessingEvent paymentEvent, string key)
    {
        if (!paymentEvent.Metadata.TryGetValue(key, out JsonElement value))
        {
            return null;
        }

        if (value.ValueKind == JsonValueKind.Number && value.TryGetInt32(out int number))
        {
            return number;
        }

        return value.ValueKind == JsonValueKind.String && int.TryParse(value.GetString(), out number)
            ? number
            : null;
    }
}
