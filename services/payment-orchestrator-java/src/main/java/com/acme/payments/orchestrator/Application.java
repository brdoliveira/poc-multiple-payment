package com.acme.payments.orchestrator;

import com.acme.payments.orchestrator.application.OutboxEventPublisher;
import com.acme.payments.orchestrator.application.PaymentOrchestratorService;
import com.acme.payments.orchestrator.application.IdempotencyStore;
import com.acme.payments.orchestrator.application.PaymentRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    PaymentOrchestratorService paymentOrchestratorService(
            PaymentRepository paymentRepository,
            IdempotencyStore idempotencyStore,
            OutboxEventPublisher outboxEventPublisher
    ) {
        return new PaymentOrchestratorService(paymentRepository, idempotencyStore, outboxEventPublisher);
    }
}
