package com.acme.payments.pixboleto.application

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.provider.PaymentProvider
import java.util.UUID

interface ChargeRepository {
    fun findByIdempotencyKey(idempotencyKey: String): ChargeResult?

    fun findFingerprint(idempotencyKey: String): String?

    fun reserve(command: ChargeCommand, provider: PaymentProvider, chargeId: UUID, requestFingerprint: String): Boolean

    fun save(command: ChargeCommand, result: ChargeResult): ChargeResult
}
