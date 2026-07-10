package com.acme.payments.pixboleto.provider

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.domain.PaymentRail

interface PaymentProviderAdapter {
    val provider: PaymentProvider

    fun supports(rail: PaymentRail): Boolean

    fun createCharge(command: ChargeCommand): ChargeResult
}
