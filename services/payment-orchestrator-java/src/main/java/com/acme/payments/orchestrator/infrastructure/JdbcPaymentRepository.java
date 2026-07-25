package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.PaymentRepository;
import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;
import com.acme.payments.orchestrator.domain.PaymentStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Payment save(Payment payment) {
        jdbcTemplate.update("""
                INSERT INTO payments (
                    id, idempotency_key, method, amount, currency, status, provider, metadata, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    status = EXCLUDED.status,
                    provider = EXCLUDED.provider,
                    metadata = EXCLUDED.metadata,
                    updated_at = EXCLUDED.updated_at
                """,
                payment.id(),
                payment.idempotencyKey(),
                payment.method().name(),
                payment.amount(),
                payment.currency(),
                payment.status().name(),
                payment.provider(),
                writeMetadata(payment.metadata()),
                Timestamp.from(payment.createdAt()),
                Timestamp.from(payment.updatedAt())
        );
        return payment;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, idempotency_key, method, amount, currency, status, provider,
                       COALESCE(metadata, '{}'::jsonb)::text AS metadata,
                       created_at, updated_at
                FROM payments
                WHERE id = ?
                """, this::mapPayment, id).stream().findFirst();
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jdbcTemplate.query("""
                SELECT id, idempotency_key, method, amount, currency, status, provider,
                       COALESCE(metadata, '{}'::jsonb)::text AS metadata,
                       created_at, updated_at
                FROM payments
                WHERE idempotency_key = ?
                """, this::mapPayment, idempotencyKey).stream().findFirst();
    }

    private Payment mapPayment(ResultSet rs, int rowNumber) throws SQLException {
        return new Payment(
                rs.getObject("id", UUID.class),
                rs.getString("idempotency_key"),
                PaymentMethod.valueOf(rs.getString("method")),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                PaymentStatus.valueOf(rs.getString("status")),
                rs.getString("provider"),
                readMetadata(rs.getString("metadata")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private String writeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize payment metadata", exception);
        }
    }

    private Map<String, Object> readMetadata(String metadata) {
        try {
            return objectMapper.readValue(metadata, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to parse payment metadata", exception);
        }
    }
}
