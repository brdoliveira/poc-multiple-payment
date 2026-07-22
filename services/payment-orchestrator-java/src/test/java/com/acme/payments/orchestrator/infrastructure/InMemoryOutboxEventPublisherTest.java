package com.acme.payments.orchestrator.infrastructure;

import com.acme.payments.orchestrator.domain.Payment;
import com.acme.payments.orchestrator.domain.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryOutboxEventPublisherTest {
    @Test
    void recordsCreatedAndProcessingEventsInOrder() {
        var outbox = new InMemoryOutboxEventPublisher();
        var payment = Payment.created("checkout-789", PaymentMethod.BOLETO, new BigDecimal("50.00"), "BRL");

        outbox.publishPaymentCreated(payment);
        outbox.publishPaymentProcessing(payment.processing("IUGU"));

        assertThat(outbox.events()).containsExactly(
                "PaymentCreated:" + payment.id(),
                "PaymentProcessing:" + payment.id()
        );
    }
}
