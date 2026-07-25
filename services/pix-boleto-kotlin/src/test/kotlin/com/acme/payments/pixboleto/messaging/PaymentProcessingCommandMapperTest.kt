package com.acme.payments.pixboleto.messaging

import com.acme.payments.pixboleto.domain.PaymentRail
import com.acme.payments.pixboleto.provider.PaymentProvider
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaymentProcessingCommandMapperTest {
    private val mapper = PaymentProcessingCommandMapper()

    @Test
    fun `maps pix event to charge command`() {
        val command = mapper.toChargeCommand(
            PaymentProcessingEvent(
                eventType = "PaymentProcessing",
                paymentId = "payment-1",
                idempotencyKey = "checkout-1",
                method = "PIX",
                amount = BigDecimal("99.90"),
                currency = "brl",
                provider = "ASAAS",
            ),
        )

        requireNotNull(command)
        assertEquals("checkout-1", command.idempotencyKey)
        assertEquals(PaymentRail.PIX, command.rail)
        assertEquals(PaymentProvider.ASAAS, command.preferredProvider)
        assertEquals("BRL", command.currency)
    }

    @Test
    fun `maps boleto metadata to charge command`() {
        val command = mapper.toChargeCommand(
            PaymentProcessingEvent(
                eventType = "PaymentProcessing",
                paymentId = "payment-2",
                idempotencyKey = "checkout-2",
                method = "BOLETO",
                amount = BigDecimal("150.00"),
                currency = "BRL",
                metadata = mapOf(
                    "preferredProvider" to "IUGU",
                    "dueDate" to "2026-07-30",
                ),
            ),
        )

        requireNotNull(command)
        assertEquals(PaymentRail.BOLETO, command.rail)
        assertEquals(PaymentProvider.IUGU, command.preferredProvider)
        assertEquals("2026-07-30", command.dueDate.toString())
    }

    @Test
    fun `ignores card events`() {
        val command = mapper.toChargeCommand(
            PaymentProcessingEvent(
                eventType = "PaymentProcessing",
                paymentId = "payment-3",
                idempotencyKey = "checkout-3",
                method = "CREDIT_CARD",
            ),
        )

        assertNull(command)
    }
}
