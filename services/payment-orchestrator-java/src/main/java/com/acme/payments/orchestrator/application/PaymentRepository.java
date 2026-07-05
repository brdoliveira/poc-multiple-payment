package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
