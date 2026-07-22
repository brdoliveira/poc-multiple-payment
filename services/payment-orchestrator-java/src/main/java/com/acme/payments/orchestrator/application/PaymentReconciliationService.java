package com.acme.payments.orchestrator.application;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentReconciliationService {
    private final JdbcTemplate jdbcTemplate;

    public PaymentReconciliationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void enqueuePayment(UUID paymentId) {
        jdbcTemplate.update("""
                INSERT INTO payment_reconciliation_jobs (
                    id, payment_id, status, attempts, next_run_at, created_at, updated_at
                )
                VALUES (?, ?, 'PENDING', 0, NOW(), NOW(), NOW())
                ON CONFLICT DO NOTHING
                """, UUID.randomUUID(), paymentId);
    }

    @Transactional
    public int enqueueStaleProcessingPayments(int olderThanMinutes) {
        return jdbcTemplate.update("""
                INSERT INTO payment_reconciliation_jobs (
                    id, payment_id, status, attempts, next_run_at, created_at, updated_at
                )
                SELECT gen_random_uuid(), id, 'PENDING', 0, NOW(), NOW(), NOW()
                FROM payments
                WHERE status = 'PROCESSING'
                  AND updated_at < NOW() - (CAST(? AS INTEGER) * INTERVAL '1 minute')
                ON CONFLICT DO NOTHING
                """, olderThanMinutes);
    }
}
