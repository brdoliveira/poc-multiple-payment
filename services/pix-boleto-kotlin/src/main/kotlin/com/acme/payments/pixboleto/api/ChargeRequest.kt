package com.acme.payments.pixboleto.api

import com.acme.payments.pixboleto.domain.PaymentRail
import com.acme.payments.pixboleto.provider.PaymentProvider
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

data class ChargeRequest(
    @field:NotBlank
    val idempotencyKey: String,
    @field:NotNull
    val rail: PaymentRail,
    @field:NotNull
    @field:DecimalMin("0.01")
    val amount: BigDecimal,
    @field:NotBlank
    @field:Size(min = 3, max = 3)
    val currency: String,
    val dueDate: LocalDate?,
    val preferredProvider: PaymentProvider?,
)
