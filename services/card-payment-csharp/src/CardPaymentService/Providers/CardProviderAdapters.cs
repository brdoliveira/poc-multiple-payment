using Acme.Payments.CardPaymentService.Domain;
using Polly;

namespace Acme.Payments.CardPaymentService.Providers;

public abstract class SimulatedCardAdapter : ICardProviderAdapter
{
    private static readonly ResiliencePipeline<CardPaymentResult> ProviderPolicy =
        new ResiliencePipelineBuilder<CardPaymentResult>()
            .AddRetry(new Polly.Retry.RetryStrategyOptions<CardPaymentResult>
            {
                MaxRetryAttempts = 2,
                Delay = TimeSpan.FromMilliseconds(200),
                BackoffType = DelayBackoffType.Exponential,
                ShouldHandle = new PredicateBuilder<CardPaymentResult>()
                    .Handle<TimeoutException>()
                    .Handle<HttpRequestException>()
            })
            .AddCircuitBreaker(new Polly.CircuitBreaker.CircuitBreakerStrategyOptions<CardPaymentResult>
            {
                FailureRatio = 0.5,
                MinimumThroughput = 10,
                SamplingDuration = TimeSpan.FromSeconds(30),
                BreakDuration = TimeSpan.FromSeconds(30),
                ShouldHandle = new PredicateBuilder<CardPaymentResult>()
                    .Handle<TimeoutException>()
                    .Handle<HttpRequestException>()
            })
            .Build();

    protected SimulatedCardAdapter(PaymentProvider provider, int maxInstallments)
    {
        Provider = provider;
        MaxInstallments = maxInstallments;
    }

    public PaymentProvider Provider { get; }

    protected int MaxInstallments { get; }

    public bool Supports(CardPaymentCommand command)
    {
        return command.Installments >= 1 && command.Installments <= MaxInstallments;
    }

    public Task<CardPaymentResult> AuthorizeAsync(CardPaymentCommand command, CancellationToken cancellationToken)
    {
        if (!Supports(command))
        {
            throw new InvalidOperationException($"{Provider} does not support {command.Installments} installments");
        }

        return ProviderPolicy.ExecuteAsync(
            _ => ValueTask.FromResult(new CardPaymentResult(
                Guid.NewGuid(),
                Provider,
                CardPaymentStatus.Authorized,
                $"{Provider}-{command.IdempotencyKey}"
            )),
            cancellationToken
        ).AsTask();
    }
}

public sealed class AsaasCardAdapter : SimulatedCardAdapter
{
    public AsaasCardAdapter() : base(PaymentProvider.Asaas, 12)
    {
    }
}

public sealed class MercadoPagoCardAdapter : SimulatedCardAdapter
{
    public MercadoPagoCardAdapter() : base(PaymentProvider.MercadoPago, 12)
    {
    }
}

public sealed class PagBankCardAdapter : SimulatedCardAdapter
{
    public PagBankCardAdapter() : base(PaymentProvider.PagBank, 18)
    {
    }
}

public sealed class IuguCardAdapter : SimulatedCardAdapter
{
    public IuguCardAdapter() : base(PaymentProvider.Iugu, 12)
    {
    }
}

public sealed class StripeCardAdapter : SimulatedCardAdapter
{
    public StripeCardAdapter() : base(PaymentProvider.Stripe, 24)
    {
    }
}
