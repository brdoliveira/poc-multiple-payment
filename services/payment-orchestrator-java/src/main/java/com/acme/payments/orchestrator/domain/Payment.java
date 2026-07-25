package com.acme.payments.orchestrator.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Payment(
        UUID id,
        String idempotencyKey,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String provider,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt
) {
    public Payment {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static Payment created(String idempotencyKey, PaymentMethod method, BigDecimal amount, String currency) {
        return created(idempotencyKey, method, amount, currency, Map.of());
    }

    public static Payment created(
            String idempotencyKey,
            PaymentMethod method,
            BigDecimal amount,
            String currency,
            Map<String, Object> metadata
    ) {
        Instant now = Instant.now();
        return new Payment(
                UUID.randomUUID(),
                idempotencyKey,
                method,
                amount,
                currency,
                PaymentStatus.CREATED,
                null,
                metadata,
                now,
                now
        );
    }

    public Payment processing(String provider) {
        return new Payment(
                id,
                idempotencyKey,
                method,
                amount,
                currency,
                PaymentStatus.PROCESSING,
                provider,
                metadata,
                createdAt,
                Instant.now()
        );
    }
}
