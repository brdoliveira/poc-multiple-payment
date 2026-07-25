package com.acme.payments.pixboleto.messaging

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.PaymentRail
import com.acme.payments.pixboleto.provider.PaymentProvider
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class PaymentProcessingCommandMapper {
    fun toChargeCommand(event: PaymentProcessingEvent): ChargeCommand? {
        val rail = when (event.method) {
            PaymentRail.PIX.name -> PaymentRail.PIX
            PaymentRail.BOLETO.name -> PaymentRail.BOLETO
            else -> return null
        }

        return ChargeCommand(
            idempotencyKey = event.idempotencyKey,
            rail = rail,
            amount = event.amount,
            currency = event.currency.uppercase(),
            dueDate = dueDate(event),
            preferredProvider = provider(event),
        )
    }

    private fun provider(event: PaymentProcessingEvent): PaymentProvider? {
        val provider = event.provider ?: event.metadata["preferredProvider"]?.toString()
        return provider?.let(PaymentProvider::valueOf)
    }

    private fun dueDate(event: PaymentProcessingEvent): LocalDate? {
        return event.metadata["dueDate"]?.toString()?.let(LocalDate::parse)
    }
}
