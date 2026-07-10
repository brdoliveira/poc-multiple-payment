package com.acme.payments.pixboleto.provider

import com.acme.payments.pixboleto.domain.PaymentRail

class ProviderRouter(
    private val adapters: List<PaymentProviderAdapter>,
) {
    fun choose(rail: PaymentRail, preferredProvider: PaymentProvider? = null): PaymentProviderAdapter {
        if (preferredProvider != null) {
            return adapters.firstOrNull { it.provider == preferredProvider && it.supports(rail) }
                ?: throw IllegalArgumentException("$preferredProvider does not support $rail")
        }

        return adapters.firstOrNull { it.supports(rail) }
            ?: throw IllegalStateException("no provider available for $rail")
    }
}
