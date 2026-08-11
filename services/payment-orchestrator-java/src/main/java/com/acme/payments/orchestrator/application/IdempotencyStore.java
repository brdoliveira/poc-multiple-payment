package com.acme.payments.orchestrator.application;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyStore {
    Optional<IdempotencyRecord> find(String key);

    boolean reserve(String key, UUID paymentId, String requestFingerprint);
}
