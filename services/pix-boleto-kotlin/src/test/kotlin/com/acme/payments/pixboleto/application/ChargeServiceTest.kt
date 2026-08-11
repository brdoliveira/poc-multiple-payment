package com.acme.payments.pixboleto.application

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.domain.PaymentRail
import com.acme.payments.pixboleto.provider.AsaasAdapter
import com.acme.payments.pixboleto.provider.PaymentProvider
import com.acme.payments.pixboleto.provider.ProviderRouter
import com.acme.payments.pixboleto.provider.PaymentProviderAdapter
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.DisplayName

class ChargeServiceTest {
    @Test
    @DisplayName("returns stored charge when idempotency key is repeated @spec:AC-019")
    fun returnsStoredChargeWhenIdempotencyKeyIsRepeated() {
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

    @Test
    @DisplayName("rejects same key with a different request @spec:AC-021")
    fun rejectsSameKeyWithDifferentRequest() {
        val repository = InMemoryChargeRepository()
        val service = ChargeService(repository, ProviderRouter(listOf(AsaasAdapter())))
        val command = ChargeCommand("pix-conflict", PaymentRail.PIX, BigDecimal("10.00"), "BRL", null, PaymentProvider.ASAAS)
        service.create(command)

        assertFailsWith<com.acme.payments.pixboleto.api.IdempotencyConflictException> {
            service.create(command.copy(amount = BigDecimal("11.00")))
        }
    }

    @Test
    @DisplayName("calls provider once for concurrent duplicates @spec:AC-019")
    fun callsProviderOnceForConcurrentDuplicates() {
        val calls = AtomicInteger()
        val adapter = object : PaymentProviderAdapter {
            override val provider = PaymentProvider.ASAAS
            override fun supports(rail: PaymentRail) = true
            override fun createCharge(command: ChargeCommand): ChargeResult {
                calls.incrementAndGet()
                return ChargeResult(UUID.randomUUID(), provider, command.rail, "PROCESSING", "external", null)
            }
        }
        val repository = InMemoryChargeRepository()
        val service = ChargeService(repository, ProviderRouter(listOf(adapter)))
        val command = ChargeCommand("pix-concurrent", PaymentRail.PIX, BigDecimal("10.00"), "BRL", null, PaymentProvider.ASAAS)
        val executor = Executors.newFixedThreadPool(6)
        try {
            val results = (1..6).map { executor.submit<ChargeResult> { service.create(command) } }.map { it.get() }
            assertEquals(1, calls.get())
            assertEquals(1, results.map { it.chargeId }.distinct().size)
        } finally {
            executor.shutdownNow()
        }
    }

    private class InMemoryChargeRepository : ChargeRepository {
        private val charges = mutableMapOf<String, ChargeResult>()
        private val fingerprints = mutableMapOf<String, String>()

        override fun findByIdempotencyKey(idempotencyKey: String): ChargeResult? = charges[idempotencyKey]

        override fun findFingerprint(idempotencyKey: String): String? = fingerprints[idempotencyKey]

        @Synchronized
        override fun reserve(command: ChargeCommand, provider: PaymentProvider, chargeId: UUID, requestFingerprint: String): Boolean {
            if (charges.containsKey(command.idempotencyKey)) return false
            fingerprints[command.idempotencyKey] = requestFingerprint
            charges[command.idempotencyKey] = ChargeResult(chargeId, provider, command.rail, "IN_PROGRESS", "pending:$chargeId", null)
            return true
        }

        @Synchronized
        override fun save(command: ChargeCommand, result: ChargeResult): ChargeResult {
            charges[command.idempotencyKey] = result
            return charges.getValue(command.idempotencyKey)
        }
    }
}
