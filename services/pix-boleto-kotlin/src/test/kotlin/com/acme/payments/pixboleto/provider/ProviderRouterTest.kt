package com.acme.payments.pixboleto.provider

import com.acme.payments.pixboleto.domain.PaymentRail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProviderRouterTest {
    private val router = ProviderRouter(
        listOf(
            AsaasAdapter(),
            MercadoPagoAdapter(),
            PagBankAdapter(),
            IuguAdapter(),
            StripeAdapter(),
        ),
    )

    @Test
    fun `uses first compatible provider when preference is absent`() {
        val adapter = router.choose(PaymentRail.PIX)

        assertEquals(PaymentProvider.ASAAS, adapter.provider)
    }

    @Test
    fun `rejects unsupported preferred provider`() {
        assertFailsWith<IllegalArgumentException> {
            router.choose(PaymentRail.BOLETO, PaymentProvider.STRIPE)
        }
    }
}
