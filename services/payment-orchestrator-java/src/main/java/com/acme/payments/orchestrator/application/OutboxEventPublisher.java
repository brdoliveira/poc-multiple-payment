package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.Payment;

public interface OutboxEventPublisher {
    void publishPaymentCreated(Payment payment);

    void publishPaymentProcessing(Payment payment);
}
