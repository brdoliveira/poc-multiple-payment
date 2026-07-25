package com.acme.payments.pixboleto.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitPaymentConsumerTopology {
    @Bean
    fun paymentEventsExchange() = TopicExchange(PAYMENT_EVENTS_EXCHANGE, true, false)

    @Bean
    fun bankRailDispatchExchange() = DirectExchange(BANK_RAIL_DISPATCH_EXCHANGE, true, false)

    @Bean
    fun bankRailRetryExchange() = DirectExchange(BANK_RAIL_RETRY_EXCHANGE, true, false)

    @Bean
    fun bankRailDeadLetterExchange() = DirectExchange(BANK_RAIL_DLX, true, false)

    @Bean
    fun bankRailPaymentEventsQueue(): Queue {
        return QueueBuilder.durable(BANK_RAIL_QUEUE)
            .deadLetterExchange(BANK_RAIL_RETRY_EXCHANGE)
            .deadLetterRoutingKey(RETRY_ROUTING_KEY)
            .build()
    }

    @Bean
    fun bankRailRetryQueue(): Queue {
        return QueueBuilder.durable(BANK_RAIL_RETRY_QUEUE)
            .ttl(RETRY_DELAY_MS)
            .deadLetterExchange(BANK_RAIL_DISPATCH_EXCHANGE)
            .deadLetterRoutingKey(DISPATCH_ROUTING_KEY)
            .build()
    }

    @Bean
    fun bankRailDeadLetterQueue(): Queue = QueueBuilder.durable(BANK_RAIL_DLQ).build()

    @Bean
    fun bankRailPaymentEventsBinding(): Binding {
        return BindingBuilder.bind(bankRailPaymentEventsQueue())
            .to(paymentEventsExchange())
            .with(PAYMENT_PROCESSING_ROUTING_KEY)
    }

    @Bean
    fun bankRailDispatchBinding(): Binding {
        return BindingBuilder.bind(bankRailPaymentEventsQueue())
            .to(bankRailDispatchExchange())
            .with(DISPATCH_ROUTING_KEY)
    }

    @Bean
    fun bankRailRetryBinding(): Binding {
        return BindingBuilder.bind(bankRailRetryQueue())
            .to(bankRailRetryExchange())
            .with(RETRY_ROUTING_KEY)
    }

    @Bean
    fun bankRailDeadLetterBinding(): Binding {
        return BindingBuilder.bind(bankRailDeadLetterQueue())
            .to(bankRailDeadLetterExchange())
            .with(DLQ_ROUTING_KEY)
    }

    companion object {
        const val PAYMENT_EVENTS_EXCHANGE = "payments.events"
        const val PAYMENT_PROCESSING_ROUTING_KEY = "PaymentProcessing"
        const val BANK_RAIL_QUEUE = "payment-events.bank-rail"
        const val BANK_RAIL_RETRY_QUEUE = "payment-events.bank-rail.retry"
        const val BANK_RAIL_DLQ = "payment-events.bank-rail.dlq"
        const val BANK_RAIL_DISPATCH_EXCHANGE = "payment-events.bank-rail.dispatch"
        const val BANK_RAIL_RETRY_EXCHANGE = "payment-events.bank-rail.retry"
        const val BANK_RAIL_DLX = "payment-events.bank-rail.dlx"
        const val DISPATCH_ROUTING_KEY = "payment"
        const val RETRY_ROUTING_KEY = "retry"
        const val DLQ_ROUTING_KEY = "dlq"
        const val RETRY_DELAY_MS = 30_000
        const val MAX_RETRY_COUNT = 3L
    }
}
