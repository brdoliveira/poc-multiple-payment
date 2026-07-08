package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;
import com.acme.payments.orchestrator.domain.PaymentStatus;
import com.acme.payments.orchestrator.infrastructure.InMemoryIdempotencyStore;
import com.acme.payments.orchestrator.infrastructure.InMemoryOutboxEventPublisher;
import com.acme.payments.orchestrator.infrastructure.InMemoryPaymentRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentOrchestratorServiceTest {
    @Test
    void returnsSamePaymentWhenIdempotencyKeyIsRepeated() {
        var repository = new InMemoryPaymentRepository();
        var outbox = new InMemoryOutboxEventPublisher();
        var service = new PaymentOrchestratorService(repository, new InMemoryIdempotencyStore(), outbox);
        var command = new CreatePaymentCommand("checkout-123", PaymentMethod.PIX, new BigDecimal("99.90"), "brl");

        Payment first = service.create(command);
        Payment second = service.create(command);

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.status()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(outbox.events()).containsExactly(
                "PaymentCreated:" + first.id(),
                "PaymentProcessing:" + first.id()
        );
    }
}
