package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.IdempotencyStore;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JdbcIdempotencyStore implements IdempotencyStore {
    private final JdbcTemplate jdbcTemplate;

    public JdbcIdempotencyStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean reserve(String key, UUID paymentId) {
        try {
            int rows = jdbcTemplate.update("""
                    INSERT INTO payment_idempotency_keys (key, payment_id, status, created_at, expires_at)
                    VALUES (?, ?, 'RESERVED', NOW(), NOW() + INTERVAL '24 hours')
                    """, key, paymentId);
            return rows == 1;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }
}
