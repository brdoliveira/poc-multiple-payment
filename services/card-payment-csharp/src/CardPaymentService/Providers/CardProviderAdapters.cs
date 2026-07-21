using Acme.Payments.CardPaymentService.Domain;

namespace Acme.Payments.CardPaymentService.Providers;

public abstract class SimulatedCardAdapter : ICardProviderAdapter
{
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

        var result = new CardPaymentResult(
            Guid.NewGuid(),
            Provider,
            CardPaymentStatus.Authorized,
            $"{Provider}-{command.IdempotencyKey}"
        );

        return Task.FromResult(result);
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
