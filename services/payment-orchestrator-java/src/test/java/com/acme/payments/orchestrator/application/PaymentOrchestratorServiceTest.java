package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;
import com.acme.payments.orchestrator.domain.PaymentStatus;
import com.acme.payments.orchestrator.infrastructure.InMemoryIdempotencyStore;
import com.acme.payments.orchestrator.infrastructure.InMemoryOutboxEventPublisher;
import com.acme.payments.orchestrator.infrastructure.InMemoryPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentOrchestratorServiceTest {
    @Test
    @DisplayName("returns same payment for concurrent duplicate requests @spec:AC-017")
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

    @Test
    @DisplayName("rejects same key with different payload @spec:AC-018 @spec:AC-021")
    void rejectsSameKeyWithDifferentPayload() {
        var repository = new InMemoryPaymentRepository();
        var service = new PaymentOrchestratorService(
                repository,
                new InMemoryIdempotencyStore(),
                new InMemoryOutboxEventPublisher()
        );

        service.create(new CreatePaymentCommand("checkout-conflict", PaymentMethod.PIX, new BigDecimal("10.00"), "BRL"));

        assertThatThrownBy(() -> service.create(
                new CreatePaymentCommand("checkout-conflict", PaymentMethod.PIX, new BigDecimal("20.00"), "BRL")
        )).isInstanceOf(com.acme.payments.orchestrator.api.IdempotencyConflictException.class);
    }

    @Test
    @DisplayName("creates only one payment for concurrent duplicate requests @spec:AC-017")
    void createsOnlyOnePaymentForConcurrentDuplicateRequests() throws Exception {
        var repository = new InMemoryPaymentRepository();
        var outbox = new InMemoryOutboxEventPublisher();
        var service = new PaymentOrchestratorService(repository, new InMemoryIdempotencyStore(), outbox);
        var command = new CreatePaymentCommand("checkout-concurrent", PaymentMethod.PIX, new BigDecimal("15.00"), "BRL");
        ExecutorService executor = Executors.newFixedThreadPool(8);

        try {
            List<Future<Payment>> futures = new ArrayList<>();
            for (int index = 0; index < 8; index++) {
                futures.add(executor.submit(() -> service.create(command)));
            }
            List<Payment> results = new ArrayList<>();
            for (Future<Payment> future : futures) {
                results.add(future.get());
            }

            assertThat(results).extracting(Payment::id).containsOnly(results.get(0).id());
            assertThat(outbox.events()).hasSize(2);
        } finally {
            executor.shutdownNow();
        }
    }
}
