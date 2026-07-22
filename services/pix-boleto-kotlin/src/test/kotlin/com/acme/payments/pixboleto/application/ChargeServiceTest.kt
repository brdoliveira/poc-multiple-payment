package com.acme.payments.pixboleto.application

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.domain.PaymentRail
import com.acme.payments.pixboleto.provider.AsaasAdapter
import com.acme.payments.pixboleto.provider.PaymentProvider
import com.acme.payments.pixboleto.provider.ProviderRouter
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ChargeServiceTest {
    @Test
    fun `returns stored charge when idempotency key is repeated`() {
        val repository = InMemoryChargeRepository()
        val service = ChargeService(repository, ProviderRouter(listOf(AsaasAdapter())))
        val command = ChargeCommand(
            idempotencyKey = "pix-123",
            rail = PaymentRail.PIX,
            amount = BigDecimal("10.00"),
            currency = "BRL",
            dueDate = null,
            preferredProvider = PaymentProvider.ASAAS,
        )

        val first = service.create(command)
        val second = service.create(command)

        assertEquals(first.chargeId, second.chargeId)
        assertEquals(PaymentProvider.ASAAS, second.provider)
    }

    private class InMemoryChargeRepository : ChargeRepository {
        private val charges = mutableMapOf<String, ChargeResult>()

        override fun findByIdempotencyKey(idempotencyKey: String): ChargeResult? = charges[idempotencyKey]

        override fun save(command: ChargeCommand, result: ChargeResult): ChargeResult {
            charges.putIfAbsent(command.idempotencyKey, result)
            return charges.getValue(command.idempotencyKey)
        }
    }
}
