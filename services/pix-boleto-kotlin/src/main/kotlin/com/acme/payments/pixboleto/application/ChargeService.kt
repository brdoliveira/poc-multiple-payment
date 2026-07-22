package com.acme.payments.pixboleto.application

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.provider.ProviderRouter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ChargeService(
    private val chargeRepository: ChargeRepository,
    private val providerRouter: ProviderRouter,
) {
    @Transactional
    fun create(command: ChargeCommand): ChargeResult {
        validate(command)
        chargeRepository.findByIdempotencyKey(command.idempotencyKey)?.let { return it }

        val result = providerRouter
            .choose(command.rail, command.preferredProvider)
            .createCharge(command)

        return chargeRepository.save(command, result)
    }

    private fun validate(command: ChargeCommand) {
        require(command.idempotencyKey.isNotBlank()) { "idempotencyKey is required" }
        require(command.amount > BigDecimal.ZERO) { "amount must be greater than zero" }
        require(command.currency.length == 3) { "currency must use ISO-4217 alpha-3 format" }
    }
}
