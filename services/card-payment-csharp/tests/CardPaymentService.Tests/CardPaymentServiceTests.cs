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

    [Fact]
    public async Task AuthorizeAsync_RejectsChangedPayloadForSameKey_spec_AC_021()
    {
        var service = new ApplicationCardPaymentService(
            new ICardProviderAdapter[] { new StripeCardAdapter() },
            new InMemoryIdempotencyStore());
        var command = new CardPaymentCommand("card-conflict", 10m, "BRL", 1, "card_token", PaymentProvider.Stripe);

        await service.AuthorizeAsync(command, CancellationToken.None);

        await Assert.ThrowsAsync<IdempotencyConflictException>(() => service.AuthorizeAsync(
            command with { Amount = 11m }, CancellationToken.None));
    }

    [Fact]
    public async Task AuthorizeAsync_InvokesProviderOnceForConcurrentDuplicates_spec_AC_020()
    {
        var service = new ApplicationCardPaymentService(
            new ICardProviderAdapter[] { new StripeCardAdapter() },
            new InMemoryIdempotencyStore());
        var command = new CardPaymentCommand("card-concurrent", 10m, "BRL", 1, "card_token", PaymentProvider.Stripe);

        Task<CardPaymentResult>[] calls = Enumerable.Range(0, 6)
            .Select(_ => service.AuthorizeAsync(command, CancellationToken.None))
            .ToArray();
        CardPaymentResult[] results = await Task.WhenAll(calls);

        Assert.Single(results.Select(result => result.PaymentId).Distinct());
    }
}
