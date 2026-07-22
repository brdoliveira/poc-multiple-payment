package com.acme.payments.orchestrator.infrastructure;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class PaymentOutboxDispatcher {
    private static final String EXCHANGE = "payments.events";

    private final JdbcTemplate jdbcTemplate;
    private final RabbitTemplate rabbitTemplate;

    public PaymentOutboxDispatcher(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${payments.outbox.dispatch-delay-ms:5000}")
    @Transactional
    public void dispatchPendingEvents() {
        List<OutboxRow> rows = jdbcTemplate.query("""
                SELECT id, event_type, payload::text AS payload
                FROM payment_outbox_events
                WHERE status = 'PENDING'
                ORDER BY created_at
                LIMIT 50
                FOR UPDATE SKIP LOCKED
                """,
                (rs, rowNumber) -> new OutboxRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("event_type"),
                        rs.getString("payload")
                )
        );

        for (OutboxRow row : rows) {
            rabbitTemplate.convertAndSend(EXCHANGE, row.eventType(), row.payload());
            jdbcTemplate.update("""
                    UPDATE payment_outbox_events
                    SET status = 'PUBLISHED', published_at = NOW()
                    WHERE id = ?
                    """, row.id());
        }
    }

    private record OutboxRow(UUID id, String eventType, String payload) {
    }
}
