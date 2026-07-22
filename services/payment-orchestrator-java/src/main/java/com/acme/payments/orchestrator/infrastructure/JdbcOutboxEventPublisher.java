package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.OutboxEventPublisher;
import com.acme.payments.orchestrator.domain.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JdbcOutboxEventPublisher implements OutboxEventPublisher {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcOutboxEventPublisher(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishPaymentCreated(Payment payment) {
        insert(payment, "PaymentCreated");
    }

    @Override
    public void publishPaymentProcessing(Payment payment) {
        insert(payment, "PaymentProcessing");
    }

    private void insert(Payment payment, String eventType) {
        jdbcTemplate.update("""
                INSERT INTO payment_outbox_events (id, aggregate_id, event_type, payload, status, created_at)
                VALUES (?, ?, ?, CAST(? AS jsonb), 'PENDING', NOW())
                """,
                UUID.randomUUID(),
                payment.id(),
                eventType,
                payload(payment, eventType)
        );
    }

    private String payload(Payment payment, String eventType) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("eventType", eventType);
            payload.put("paymentId", payment.id().toString());
            payload.put("method", payment.method().name());
            payload.put("amount", payment.amount());
            payload.put("currency", payment.currency());
            payload.put("occurredAt", Instant.now().toString());
            payload.put("version", 1);
            payload.put("metadata", Map.of("status", payment.status().name()));
            if (payment.provider() != null) {
                payload.put("provider", payment.provider());
            }
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize outbox event", exception);
        }
    }
}
