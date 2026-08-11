package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.IdempotencyStore;
import com.acme.payments.orchestrator.application.IdempotencyRecord;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcIdempotencyStore implements IdempotencyStore {
    private final JdbcTemplate jdbcTemplate;

    public JdbcIdempotencyStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<IdempotencyRecord> find(String key) {
        return jdbcTemplate.query("""
                        SELECT payment_id, request_fingerprint, status
                        FROM payment_idempotency_keys
                        WHERE key = ?
                        """,
                (rs, rowNumber) -> new IdempotencyRecord(
                        rs.getObject("payment_id", UUID.class),
                        rs.getString("request_fingerprint"),
                        rs.getString("status")
                ),
                key
        ).stream().findFirst();
    }

    @Override
    public boolean reserve(String key, UUID paymentId, String requestFingerprint) {
        try {
            int rows = jdbcTemplate.update("""
                    INSERT INTO payment_idempotency_keys (key, payment_id, request_fingerprint, status, created_at, expires_at)
                    VALUES (?, ?, ?, 'RESERVED', NOW(), NOW() + INTERVAL '24 hours')
                    """, key, paymentId, requestFingerprint);
            return rows == 1;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }
}
