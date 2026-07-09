package com.acme.payments.pixboleto.domain

import com.acme.payments.pixboleto.provider.PaymentProvider
import java.util.UUID

data class ChargeResult(
    val chargeId: UUID,
    val provider: PaymentProvider,
    val rail: PaymentRail,
    val status: String,
    val externalReference: String,
    val customerAction: String?,
)
