package com.acme.payments.orchestrator.application;

import java.util.UUID;

public interface IdempotencyStore {
    boolean reserve(String key, UUID paymentId);
}
