package com.acme.payments.orchestrator.application;

import java.util.UUID;

public record IdempotencyRecord(
        UUID paymentId,
        String requestFingerprint,
        String status
) {
}
