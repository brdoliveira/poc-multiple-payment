package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.PaymentMethod;

import java.math.BigDecimal;

public record CreatePaymentCommand(
        String idempotencyKey,
        PaymentMethod method,
        BigDecimal amount,
        String currency
) {
}
