package com.acme.payments.pixboleto.messaging

import com.acme.payments.pixboleto.application.ChargeService
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class PaymentEventListener(
    private val objectMapper: ObjectMapper,
    private val commandMapper: PaymentProcessingCommandMapper,
    private val chargeService: ChargeService,
    private val rabbitTemplate: RabbitTemplate,
) {
    @RabbitListener(
        queues = [RabbitPaymentConsumerTopology.BANK_RAIL_QUEUE],
        ackMode = "MANUAL",
    )
    fun handle(rawPayload: String, message: Message, channel: Channel) {
        val deliveryTag = message.messageProperties.deliveryTag
        try {
            val event = objectMapper.readValue(rawPayload, PaymentProcessingEvent::class.java)
            if (event.eventType == RabbitPaymentConsumerTopology.PAYMENT_PROCESSING_ROUTING_KEY) {
                commandMapper.toChargeCommand(event)?.let(chargeService::create)
            }
            channel.basicAck(deliveryTag, false)
        } catch (exception: Exception) {
            if (shouldDeadLetter(message)) {
                rabbitTemplate.convertAndSend(
                    RabbitPaymentConsumerTopology.BANK_RAIL_DLX,
                    RabbitPaymentConsumerTopology.DLQ_ROUTING_KEY,
                    rawPayload,
                )
                channel.basicAck(deliveryTag, false)
                return
            }

            channel.basicReject(deliveryTag, false)
        }
    }

    private fun shouldDeadLetter(message: Message): Boolean {
        return RabbitRetrySupport.deadLettersFromQueue(
            message,
            RabbitPaymentConsumerTopology.BANK_RAIL_QUEUE,
        ) >= RabbitPaymentConsumerTopology.MAX_RETRY_COUNT
    }
}
