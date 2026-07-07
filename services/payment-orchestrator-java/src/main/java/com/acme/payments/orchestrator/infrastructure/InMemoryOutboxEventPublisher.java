package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.application.OutboxEventPublisher;
import com.acme.payments.orchestrator.domain.Payment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryOutboxEventPublisher implements OutboxEventPublisher {
    private final List<String> events = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void publishPaymentCreated(Payment payment) {
        events.add("PaymentCreated:" + payment.id());
    }

    @Override
    public void publishPaymentProcessing(Payment payment) {
        events.add("PaymentProcessing:" + payment.id());
    }

    public List<String> events() {
        return List.copyOf(events);
    }
}
