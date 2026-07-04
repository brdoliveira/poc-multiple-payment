package com.acme.payments.orchestrator;

import com.acme.payments.orchestrator.application.OutboxEventPublisher;
import com.acme.payments.orchestrator.application.PaymentOrchestratorService;
import com.acme.payments.orchestrator.infrastructure.InMemoryIdempotencyStore;
import com.acme.payments.orchestrator.infrastructure.InMemoryOutboxEventPublisher;
import com.acme.payments.orchestrator.infrastructure.InMemoryPaymentRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    InMemoryPaymentRepository paymentRepository() {
        return new InMemoryPaymentRepository();
    }

    @Bean
    InMemoryIdempotencyStore idempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    @Bean
    OutboxEventPublisher outboxEventPublisher() {
        return new InMemoryOutboxEventPublisher();
    }

    @Bean
    PaymentOrchestratorService paymentOrchestratorService(
            InMemoryPaymentRepository paymentRepository,
            InMemoryIdempotencyStore idempotencyStore,
            OutboxEventPublisher outboxEventPublisher
    ) {
        return new PaymentOrchestratorService(paymentRepository, idempotencyStore, outboxEventPublisher);
    }
}
