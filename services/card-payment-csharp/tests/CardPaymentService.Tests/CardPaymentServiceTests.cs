using Acme.Payments.CardPaymentService.Application;
using Acme.Payments.CardPaymentService.Domain;
using Acme.Payments.CardPaymentService.Infrastructure;
using Acme.Payments.CardPaymentService.Providers;
using Xunit;

namespace CardPaymentService.Tests;

public sealed class CardPaymentServiceTests
{
    [Fact]
    public async Task AuthorizeAsync_ReusesExistingAuthorization_WhenIdempotencyKeyIsRepeated()
    {
        var service = new ApplicationCardPaymentService(
            new ICardProviderAdapter[]
            {
                new AsaasCardAdapter(),
                new MercadoPagoCardAdapter(),
                new PagBankCardAdapter(),
                new IuguCardAdapter(),
                new StripeCardAdapter()
            },
            new InMemoryIdempotencyStore()
        );
        var command = new CardPaymentCommand("checkout-456", 199.90m, "BRL", 3, "card_token", PaymentProvider.Stripe);

        CardPaymentResult first = await service.AuthorizeAsync(command, CancellationToken.None);
        CardPaymentResult second = await service.AuthorizeAsync(command, CancellationToken.None);

        Assert.Equal(first.PaymentId, second.PaymentId);
        Assert.Equal(PaymentProvider.Stripe, second.Provider);
        Assert.Equal(CardPaymentStatus.Authorized, second.Status);
    }
}
