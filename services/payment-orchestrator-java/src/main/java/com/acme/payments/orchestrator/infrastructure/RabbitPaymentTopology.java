package com.acme.payments.orchestrator.infrastructure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPaymentTopology {
    public static final String PAYMENT_EVENTS_EXCHANGE = "payments.events";
    public static final String PAYMENT_PROCESSING_ROUTING_KEY = "PaymentProcessing";
    private static final int RETRY_DELAY_MS = 30_000;

    @Bean
    TopicExchange paymentEventsExchange() {
        return new TopicExchange(PAYMENT_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    Queue paymentEventsQueue() {
        return new Queue("payment-events", true);
    }

    @Bean
    Binding paymentEventsBinding(
            @Qualifier("paymentEventsQueue") Queue paymentEventsQueue,
            TopicExchange paymentEventsExchange
    ) {
        return BindingBuilder.bind(paymentEventsQueue).to(paymentEventsExchange).with("#");
    }

    @Bean
    DirectExchange bankRailDispatchExchange() {
        return new DirectExchange("payment-events.bank-rail.dispatch", true, false);
    }

    @Bean
    DirectExchange bankRailRetryExchange() {
        return new DirectExchange("payment-events.bank-rail.retry", true, false);
    }

    @Bean
    DirectExchange bankRailDeadLetterExchange() {
        return new DirectExchange("payment-events.bank-rail.dlx", true, false);
    }

    @Bean
    Queue bankRailPaymentEventsQueue() {
        return QueueBuilder.durable("payment-events.bank-rail")
                .deadLetterExchange("payment-events.bank-rail.retry")
                .deadLetterRoutingKey("retry")
                .build();
    }

    @Bean
    Queue bankRailRetryQueue() {
        return QueueBuilder.durable("payment-events.bank-rail.retry")
                .ttl(RETRY_DELAY_MS)
                .deadLetterExchange("payment-events.bank-rail.dispatch")
                .deadLetterRoutingKey("payment")
                .build();
    }

    @Bean
    Queue bankRailDeadLetterQueue() {
        return QueueBuilder.durable("payment-events.bank-rail.dlq").build();
    }

    @Bean
    Binding bankRailPaymentEventsBinding(
            @Qualifier("bankRailPaymentEventsQueue") Queue bankRailPaymentEventsQueue,
            TopicExchange paymentEventsExchange
    ) {
        return BindingBuilder.bind(bankRailPaymentEventsQueue)
                .to(paymentEventsExchange)
                .with(PAYMENT_PROCESSING_ROUTING_KEY);
    }

    @Bean
    Binding bankRailDispatchBinding(
            @Qualifier("bankRailPaymentEventsQueue") Queue bankRailPaymentEventsQueue,
            DirectExchange bankRailDispatchExchange
    ) {
        return BindingBuilder.bind(bankRailPaymentEventsQueue).to(bankRailDispatchExchange).with("payment");
    }

    @Bean
    Binding bankRailRetryBinding(
            @Qualifier("bankRailRetryQueue") Queue bankRailRetryQueue,
            DirectExchange bankRailRetryExchange
    ) {
        return BindingBuilder.bind(bankRailRetryQueue).to(bankRailRetryExchange).with("retry");
    }

    @Bean
    Binding bankRailDeadLetterBinding(
            @Qualifier("bankRailDeadLetterQueue") Queue bankRailDeadLetterQueue,
            DirectExchange bankRailDeadLetterExchange
    ) {
        return BindingBuilder.bind(bankRailDeadLetterQueue).to(bankRailDeadLetterExchange).with("dlq");
    }

    @Bean
    DirectExchange cardDispatchExchange() {
        return new DirectExchange("payment-events.card.dispatch", true, false);
    }

    @Bean
    DirectExchange cardRetryExchange() {
        return new DirectExchange("payment-events.card.retry", true, false);
    }

    @Bean
    DirectExchange cardDeadLetterExchange() {
        return new DirectExchange("payment-events.card.dlx", true, false);
    }

    @Bean
    Queue cardPaymentEventsQueue() {
        return QueueBuilder.durable("payment-events.card")
                .deadLetterExchange("payment-events.card.retry")
                .deadLetterRoutingKey("retry")
                .build();
    }

    @Bean
    Queue cardRetryQueue() {
        return QueueBuilder.durable("payment-events.card.retry")
                .ttl(RETRY_DELAY_MS)
                .deadLetterExchange("payment-events.card.dispatch")
                .deadLetterRoutingKey("payment")
                .build();
    }

    @Bean
    Queue cardDeadLetterQueue() {
        return QueueBuilder.durable("payment-events.card.dlq").build();
    }

    @Bean
    Binding cardPaymentEventsBinding(
            @Qualifier("cardPaymentEventsQueue") Queue cardPaymentEventsQueue,
            TopicExchange paymentEventsExchange
    ) {
        return BindingBuilder.bind(cardPaymentEventsQueue)
                .to(paymentEventsExchange)
                .with(PAYMENT_PROCESSING_ROUTING_KEY);
    }

    @Bean
    Binding cardDispatchBinding(
            @Qualifier("cardPaymentEventsQueue") Queue cardPaymentEventsQueue,
            DirectExchange cardDispatchExchange
    ) {
        return BindingBuilder.bind(cardPaymentEventsQueue).to(cardDispatchExchange).with("payment");
    }

    @Bean
    Binding cardRetryBinding(
            @Qualifier("cardRetryQueue") Queue cardRetryQueue,
            DirectExchange cardRetryExchange
    ) {
        return BindingBuilder.bind(cardRetryQueue).to(cardRetryExchange).with("retry");
    }

    @Bean
    Binding cardDeadLetterBinding(
            @Qualifier("cardDeadLetterQueue") Queue cardDeadLetterQueue,
            DirectExchange cardDeadLetterExchange
    ) {
        return BindingBuilder.bind(cardDeadLetterQueue).to(cardDeadLetterExchange).with("dlq");
    }
}
