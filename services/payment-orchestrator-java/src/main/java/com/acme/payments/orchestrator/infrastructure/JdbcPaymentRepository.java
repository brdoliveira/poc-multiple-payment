package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.PaymentRepository;
import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;
import com.acme.payments.orchestrator.domain.PaymentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Payment save(Payment payment) {
        jdbcTemplate.update("""
                INSERT INTO payments (
                    id, idempotency_key, method, amount, currency, status, provider, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    status = EXCLUDED.status,
                    provider = EXCLUDED.provider,
                    updated_at = EXCLUDED.updated_at
                """,
                payment.id(),
                payment.idempotencyKey(),
                payment.method().name(),
                payment.amount(),
                payment.currency(),
                payment.status().name(),
                payment.provider(),
                Timestamp.from(payment.createdAt()),
                Timestamp.from(payment.updatedAt())
        );
        return payment;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, idempotency_key, method, amount, currency, status, provider, created_at, updated_at
                FROM payments
                WHERE id = ?
                """, this::mapPayment, id).stream().findFirst();
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jdbcTemplate.query("""
                SELECT id, idempotency_key, method, amount, currency, status, provider, created_at, updated_at
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
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
