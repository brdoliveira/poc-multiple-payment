using System.Text.Json;
using Acme.Payments.CardPaymentService.Domain;
using Acme.Payments.CardPaymentService.Messaging;
using Xunit;

namespace CardPaymentService.Tests;

public sealed class PaymentProcessingCommandMapperTests
{
    [Fact]
    public void ToCommand_MapsCreditCardEvent()
    {
        var mapper = new PaymentProcessingCommandMapper();
        var paymentEvent = new PaymentProcessingEvent
        {
            EventType = RabbitPaymentTopology.PaymentProcessingRoutingKey,
            PaymentId = "payment-1",
            IdempotencyKey = "checkout-1",
            Method = "CREDIT_CARD",
            Amount = 199.90m,
            Currency = "brl",
            Provider = "STRIPE",
            Metadata = JsonSerializer.Deserialize<Dictionary<string, JsonElement>>(
                """{"installments":3,"cardToken":"tok_test"}"""
            )!
        };

        CardPaymentCommand? command = mapper.ToCommand(paymentEvent);

        Assert.NotNull(command);
        Assert.Equal("checkout-1", command.IdempotencyKey);
        Assert.Equal(3, command.Installments);
        Assert.Equal("tok_test", command.CardToken);
        Assert.Equal(PaymentProvider.Stripe, command.PreferredProvider);
        Assert.Equal("BRL", command.Currency);
    }

    [Fact]
    public void ToCommand_IgnoresPixEvent()
    {
        var mapper = new PaymentProcessingCommandMapper();
        var paymentEvent = new PaymentProcessingEvent
        {
            EventType = RabbitPaymentTopology.PaymentProcessingRoutingKey,
            Method = "PIX"
        };

        Assert.Null(mapper.ToCommand(paymentEvent));
    }
}
