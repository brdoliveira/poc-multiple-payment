package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.IdempotencyStore;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Set<String> reservedKeys = ConcurrentHashMap.newKeySet();

    @Override
    public boolean reserve(String key, UUID paymentId) {
        return reservedKeys.add(key);
    }
}
