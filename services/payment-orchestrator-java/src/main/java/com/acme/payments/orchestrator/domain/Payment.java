package com.acme.payments.orchestrator.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        String idempotencyKey,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String provider,
        Instant createdAt,
        Instant updatedAt
) {
    public static Payment created(String idempotencyKey, PaymentMethod method, BigDecimal amount, String currency) {
        Instant now = Instant.now();
        return new Payment(
                UUID.randomUUID(),
                idempotencyKey,
                method,
                amount,
                currency,
                PaymentStatus.CREATED,
                null,
                now,
                now
        );
    }

    public Payment processing(String provider) {
        return new Payment(id, idempotencyKey, method, amount, currency, PaymentStatus.PROCESSING, provider, createdAt, Instant.now());
    }
}
