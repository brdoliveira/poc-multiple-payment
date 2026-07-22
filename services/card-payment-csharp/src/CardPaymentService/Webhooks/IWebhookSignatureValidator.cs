namespace Acme.Payments.CardPaymentService.Webhooks;

public interface IWebhookSignatureValidator
{
    bool IsValid(string provider, string payload, string signature);
}
