package com.acme.payments.pixboleto.application

import com.acme.payments.pixboleto.api.IdempotencyConflictException
import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.provider.ProviderRouter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class ChargeService(
    private val chargeRepository: ChargeRepository,
    private val providerRouter: ProviderRouter,
    private val meterRegistry: MeterRegistry = Metrics.globalRegistry,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val locks = ConcurrentHashMap<String, Any>()

    fun create(command: ChargeCommand): ChargeResult {
        validate(command)
        val fingerprint = RequestFingerprint.of(command)
        val timer = Timer.start(meterRegistry)

        synchronized(locks.computeIfAbsent(command.idempotencyKey) { Any() }) {
            try {
                chargeRepository.findByIdempotencyKey(command.idempotencyKey)?.let {
                    ensureSameRequest(command, fingerprint)
                    meterRegistry.counter("payments.idempotency.reused", "service", "pix-boleto").increment()
                    return it
                }

                val adapter = providerRouter.choose(command.rail, command.preferredProvider)
                val reservationId = UUID.randomUUID()
                if (!chargeRepository.reserve(command, adapter.provider, reservationId, fingerprint)) {
                    ensureSameRequest(command, fingerprint)
                    return chargeRepository.findByIdempotencyKey(command.idempotencyKey)
                        ?: error("idempotency reservation has no charge result")
                }

                val result = adapter.createCharge(command).copy(chargeId = reservationId)
                val completed = chargeRepository.save(command, result)
                logger.info(
                    "payment_outcome service=pix-boleto operation=charge outcome=completed charge_id={} idempotency_key_hash={} rail={} provider={} status={}",
                    completed.chargeId,
                    fingerprint.take(12),
                    completed.rail,
                    completed.provider,
                    completed.status,
                )
                meterRegistry.counter("payments.completed", "service", "pix-boleto", "rail", completed.rail.name).increment()
                return completed
            } catch (exception: IdempotencyConflictException) {
                meterRegistry.counter("payments.idempotency.conflicts", "service", "pix-boleto").increment()
                logger.warn("payment_outcome service=pix-boleto operation=charge outcome=idempotency_conflict idempotency_key_hash={}", fingerprint.take(12))
                throw exception
            } finally {
                timer.stop(meterRegistry.timer("payments.charge.duration", "service", "pix-boleto"))
            }
        }
    }

    private fun ensureSameRequest(command: ChargeCommand, fingerprint: String) {
        val stored = chargeRepository.findFingerprint(command.idempotencyKey)
        if (stored != null && stored != fingerprint) {
            throw IdempotencyConflictException(command.idempotencyKey)
        }
    }

    private fun validate(command: ChargeCommand) {
        require(command.idempotencyKey.isNotBlank()) { "idempotencyKey is required" }
        require(command.amount > BigDecimal.ZERO) { "amount must be greater than zero" }
        require(command.currency.length == 3) { "currency must use ISO-4217 alpha-3 format" }
    }
}
