package com.acme.payments.pixboleto.messaging

import java.math.BigDecimal

data class PaymentProcessingEvent(
    val eventId: String = "",
    val eventType: String = "",
    val paymentId: String = "",
    val idempotencyKey: String = "",
    val method: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val currency: String = "",
    val provider: String? = null,
    val metadata: Map<String, Any?> = emptyMap(),
)
