package com.acme.payments.pixboleto.provider

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.domain.PaymentRail
import java.util.UUID

abstract class BankRailAdapter(
    override val provider: PaymentProvider,
) : PaymentProviderAdapter {
    override fun supports(rail: PaymentRail) = rail == PaymentRail.PIX || rail == PaymentRail.BOLETO

    override fun createCharge(command: ChargeCommand): ChargeResult {
        require(supports(command.rail)) { "$provider does not support ${command.rail}" }
        val action = when (command.rail) {
            PaymentRail.PIX -> "pix-qrcode:${provider.name.lowercase()}:${command.idempotencyKey}"
            PaymentRail.BOLETO -> "boleto-url:${provider.name.lowercase()}:${command.idempotencyKey}"
        }
        return ChargeResult(
            chargeId = UUID.randomUUID(),
            provider = provider,
            rail = command.rail,
            status = "PROCESSING",
            externalReference = "${provider.name}-${command.idempotencyKey}",
            customerAction = action,
        )
    }
}

class AsaasAdapter : BankRailAdapter(PaymentProvider.ASAAS)

class MercadoPagoAdapter : BankRailAdapter(PaymentProvider.MERCADO_PAGO)

class PagBankAdapter : BankRailAdapter(PaymentProvider.PAGBANK)

class IuguAdapter : BankRailAdapter(PaymentProvider.IUGU)

class StripeAdapter : PaymentProviderAdapter {
    override val provider = PaymentProvider.STRIPE

    override fun supports(rail: PaymentRail) = false

    override fun createCharge(command: ChargeCommand): ChargeResult {
        throw UnsupportedOperationException("Stripe is handled by the card service in this PoC")
    }
}
