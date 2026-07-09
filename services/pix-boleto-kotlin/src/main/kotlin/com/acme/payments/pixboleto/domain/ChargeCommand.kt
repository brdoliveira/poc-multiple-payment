package com.acme.payments.pixboleto.domain

import com.acme.payments.pixboleto.provider.PaymentProvider
import java.math.BigDecimal
import java.time.LocalDate

data class ChargeCommand(
    val idempotencyKey: String,
    val rail: PaymentRail,
    val amount: BigDecimal,
    val currency: String,
    val dueDate: LocalDate?,
    val preferredProvider: PaymentProvider?,
)
