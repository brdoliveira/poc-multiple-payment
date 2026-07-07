package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.PaymentRepository;
import com.acme.payments.orchestrator.domain.Payment;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<UUID, Payment> paymentsById = new ConcurrentHashMap<>();
    private final Map<String, UUID> paymentsByIdempotencyKey = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        paymentsById.put(payment.id(), payment);
        paymentsByIdempotencyKey.put(payment.idempotencyKey(), payment.id());
        return payment;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return Optional.ofNullable(paymentsById.get(id));
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        UUID paymentId = paymentsByIdempotencyKey.get(idempotencyKey);
        return paymentId == null ? Optional.empty() : findById(paymentId);
    }
}
