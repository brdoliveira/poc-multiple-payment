package com.acme.payments.pixboleto.application

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult

interface ChargeRepository {
    fun findByIdempotencyKey(idempotencyKey: String): ChargeResult?

    fun save(command: ChargeCommand, result: ChargeResult): ChargeResult
}
