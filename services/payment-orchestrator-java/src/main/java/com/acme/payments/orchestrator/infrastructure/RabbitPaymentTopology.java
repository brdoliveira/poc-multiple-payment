package com.acme.payments.orchestrator.infrastructure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPaymentTopology {
    @Bean
    TopicExchange paymentEventsExchange() {
        return new TopicExchange("payments.events", true, false);
    }

    @Bean
    Queue paymentEventsQueue() {
        return new Queue("payment-events", true);
    }

    @Bean
    Binding paymentEventsBinding(Queue paymentEventsQueue, TopicExchange paymentEventsExchange) {
        return BindingBuilder.bind(paymentEventsQueue).to(paymentEventsExchange).with("#");
    }
}
