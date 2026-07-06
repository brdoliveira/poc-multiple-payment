package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public class PaymentOrchestratorService {
    private final PaymentRepository paymentRepository;
    private final IdempotencyStore idempotencyStore;
    private final OutboxEventPublisher outboxEventPublisher;

    public PaymentOrchestratorService(
            PaymentRepository paymentRepository,
            IdempotencyStore idempotencyStore,
            OutboxEventPublisher outboxEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.idempotencyStore = idempotencyStore;
        this.outboxEventPublisher = outboxEventPublisher;
    }

    public Payment create(CreatePaymentCommand command) {
        validate(command);

        return paymentRepository.findByIdempotencyKey(command.idempotencyKey())
                .orElseGet(() -> createNewPayment(command));
    }

    private Payment createNewPayment(CreatePaymentCommand command) {
        Payment created = Payment.created(
                command.idempotencyKey(),
                command.method(),
                command.amount(),
                command.currency().toUpperCase(Locale.ROOT)
        );

        boolean reserved = idempotencyStore.reserve(command.idempotencyKey(), created.id());
        if (!reserved) {
            return paymentRepository.findByIdempotencyKey(command.idempotencyKey())
                    .orElseThrow(() -> new IllegalStateException("idempotency key reserved without payment record"));
        }

        paymentRepository.save(created);
        outboxEventPublisher.publishPaymentCreated(created);

        Payment processing = created.processing(defaultProviderFor(command.method()));
        paymentRepository.save(processing);
        outboxEventPublisher.publishPaymentProcessing(processing);
        return processing;
    }

    private void validate(CreatePaymentCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
        if (command.method() == null) {
            throw new IllegalArgumentException("payment method is required");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (command.currency() == null || command.currency().length() != 3) {
            throw new IllegalArgumentException("currency must use ISO-4217 alpha-3 format");
        }
    }

    private String defaultProviderFor(PaymentMethod method) {
        return switch (method) {
            case PIX -> "ASAAS";
            case BOLETO -> "IUGU";
            case CREDIT_CARD -> "STRIPE";
        };
    }
}
