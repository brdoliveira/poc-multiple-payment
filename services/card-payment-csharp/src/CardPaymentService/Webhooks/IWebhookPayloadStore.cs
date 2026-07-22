namespace Acme.Payments.CardPaymentService.Webhooks;

public interface IWebhookPayloadStore
{
    Task SaveAsync(ProviderWebhookPayload payload, CancellationToken cancellationToken);
}
