using Acme.Payments.CardPaymentService.Webhooks;
using Xunit;

namespace CardPaymentService.Tests;

public sealed class WebhookPayloadTests
{
    [Fact]
    public void From_GeneratesStableExternalEventId_ForSameProviderAndPayload()
    {
        ProviderWebhookPayload first = ProviderWebhookPayload.From("asaas", "{\"id\":\"evt_1\"}", "signature");
        ProviderWebhookPayload second = ProviderWebhookPayload.From("asaas", "{\"id\":\"evt_1\"}", "signature");

        Assert.Equal(first.ExternalEventId, second.ExternalEventId);
    }
}
