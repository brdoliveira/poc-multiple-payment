package com.acme.payments.orchestrator.api;

import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;
import com.acme.payments.orchestrator.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String provider,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.id(),
                payment.method(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                payment.provider(),
                payment.updatedAt()
        );
    }
}
