using System.Security.Cryptography;
using System.Text;

namespace Acme.Payments.CardPaymentService.Webhooks;

public sealed record ProviderWebhookPayload(
    string Provider,
    string ExternalEventId,
    string Payload,
    string Signature,
    DateTimeOffset ReceivedAt
)
{
    public static ProviderWebhookPayload From(string provider, string payload, string signature)
    {
        return new ProviderWebhookPayload(
            Provider: provider,
            ExternalEventId: ComputeEventId(provider, payload),
            Payload: payload,
            Signature: signature,
            ReceivedAt: DateTimeOffset.UtcNow
        );
    }

    private static string ComputeEventId(string provider, string payload)
    {
        byte[] bytes = SHA256.HashData(Encoding.UTF8.GetBytes($"{provider}:{payload}"));
        return Convert.ToHexString(bytes).ToLowerInvariant();
    }
}
