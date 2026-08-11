package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.IdempotencyStore;

import com.acme.payments.orchestrator.application.IdempotencyRecord;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Map<String, IdempotencyRecord> records = new ConcurrentHashMap<>();

    @Override
    public Optional<IdempotencyRecord> find(String key) {
        return Optional.ofNullable(records.get(key));
    }

    @Override
    public boolean reserve(String key, UUID paymentId, String requestFingerprint) {
        return records.putIfAbsent(key, new IdempotencyRecord(paymentId, requestFingerprint, "RESERVED")) == null;
    }
}
