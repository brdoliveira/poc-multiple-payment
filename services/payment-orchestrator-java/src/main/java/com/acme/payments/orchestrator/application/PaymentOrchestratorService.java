package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;
import com.acme.payments.orchestrator.api.IdempotencyConflictException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.transaction.annotation.Transactional;

public class PaymentOrchestratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOrchestratorService.class);

    private final PaymentRepository paymentRepository;
    private final IdempotencyStore idempotencyStore;
    private final OutboxEventPublisher outboxEventPublisher;
    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Object> idempotencyLocks = new ConcurrentHashMap<>();

    public PaymentOrchestratorService(
            PaymentRepository paymentRepository,
            IdempotencyStore idempotencyStore,
            OutboxEventPublisher outboxEventPublisher
    ) {
        this(paymentRepository, idempotencyStore, outboxEventPublisher, Metrics.globalRegistry);
    }

    public PaymentOrchestratorService(
            PaymentRepository paymentRepository,
            IdempotencyStore idempotencyStore,
            OutboxEventPublisher outboxEventPublisher,
            MeterRegistry meterRegistry
    ) {
        this.paymentRepository = paymentRepository;
        this.idempotencyStore = idempotencyStore;
        this.outboxEventPublisher = outboxEventPublisher;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public Payment create(CreatePaymentCommand command) {
        validate(command);
        String fingerprint = RequestFingerprint.of(command);
        Timer.Sample timer = Timer.start(meterRegistry);

        synchronized (idempotencyLocks.computeIfAbsent(command.idempotencyKey(), ignored -> new Object())) {
            try {
                Optional<IdempotencyRecord> record = idempotencyStore.find(command.idempotencyKey());
                if (record.isPresent()) {
                    ensureSameRequest(command, record.get(), fingerprint);
                    Payment payment = paymentRepository.findById(record.get().paymentId())
                            .orElseThrow(() -> new IllegalStateException("idempotency record has no payment"));
                    LOGGER.info("payment_outcome service=payment-orchestrator operation=create outcome=reused payment_id={} idempotency_key_hash={} method={} amount={} currency={} status={}",
                            payment.id(), shortHash(fingerprint), payment.method(), payment.amount(), payment.currency(), payment.status());
                    meterRegistry.counter("payments.idempotency.reused", "service", "payment-orchestrator").increment();
                    return payment;
                }

                Payment payment = paymentRepository.findByIdempotencyKey(command.idempotencyKey())
                        .orElseGet(() -> createNewPayment(command, fingerprint));
                LOGGER.info("payment_outcome service=payment-orchestrator operation=create outcome=created payment_id={} idempotency_key_hash={} method={} amount={} currency={} status={}",
                        payment.id(), shortHash(fingerprint), payment.method(), payment.amount(), payment.currency(), payment.status());
                meterRegistry.counter("payments.created", "service", "payment-orchestrator").increment();
                return payment;
            } catch (IdempotencyConflictException exception) {
                meterRegistry.counter("payments.idempotency.conflicts", "service", "payment-orchestrator").increment();
                LOGGER.warn("payment_outcome service=payment-orchestrator operation=create outcome=idempotency_conflict idempotency_key_hash={}", shortHash(fingerprint));
                throw exception;
            } finally {
                timer.stop(meterRegistry.timer("payments.create.duration", "service", "payment-orchestrator"));
            }
        }
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    private Payment createNewPayment(CreatePaymentCommand command, String fingerprint) {
        Payment created = Payment.created(
                command.idempotencyKey(),
                command.method(),
                command.amount(),
                command.currency().toUpperCase(Locale.ROOT),
                command.metadata()
        );

        boolean reserved = idempotencyStore.reserve(command.idempotencyKey(), created.id(), fingerprint);
        if (!reserved) {
            IdempotencyRecord record = idempotencyStore.find(command.idempotencyKey())
                    .orElseThrow(() -> new IllegalStateException("idempotency key reserved without record"));
            ensureSameRequest(command, record, fingerprint);
            return paymentRepository.findById(record.paymentId())
                    .orElseThrow(() -> new IllegalStateException("idempotency key reserved without payment record"));
        }

        paymentRepository.save(created);
        outboxEventPublisher.publishPaymentCreated(created);

        Payment processing = created.processing(defaultProviderFor(command.method()));
        paymentRepository.save(processing);
        outboxEventPublisher.publishPaymentProcessing(processing);
        return processing;
    }

    private void ensureSameRequest(CreatePaymentCommand command, IdempotencyRecord record, String fingerprint) {
        if (!Objects.equals(record.requestFingerprint(), fingerprint)) {
            throw new IdempotencyConflictException(command.idempotencyKey());
        }
    }

    private static String shortHash(String value) {
        return value.substring(0, Math.min(12, value.length()));
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
