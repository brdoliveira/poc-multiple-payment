package com.acme.payments.pixboleto.messaging

import com.acme.payments.pixboleto.application.ChargeService
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PaymentEventListener(
    private val objectMapper: ObjectMapper,
    private val commandMapper: PaymentProcessingCommandMapper,
    private val chargeService: ChargeService,
    private val rabbitTemplate: RabbitTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(
        queues = [RabbitPaymentConsumerTopology.BANK_RAIL_QUEUE],
        ackMode = "MANUAL",
    )
    fun handle(rawPayload: String, message: Message, channel: Channel) {
        val deliveryTag = message.messageProperties.deliveryTag
        var correlationId = ""
        try {
            val event = objectMapper.readValue(rawPayload, PaymentProcessingEvent::class.java)
            correlationId = safeCorrelationId(event.correlationId) ?: UUID.randomUUID().toString()
            MDC.put("correlationId", correlationId)
            if (event.eventType == RabbitPaymentConsumerTopology.PAYMENT_PROCESSING_ROUTING_KEY) {
                commandMapper.toChargeCommand(event)?.let(chargeService::create)
            }
            logger.info("payment_event outcome=processed service=pix-boleto event_type={} payment_id={} correlation_id={}", event.eventType, event.paymentId, correlationId)
            channel.basicAck(deliveryTag, false)
        } catch (exception: Exception) {
            if (shouldDeadLetter(message)) {
                rabbitTemplate.convertAndSend(
                    RabbitPaymentConsumerTopology.BANK_RAIL_DLX,
                    RabbitPaymentConsumerTopology.DLQ_ROUTING_KEY,
                    rawPayload,
                )
                channel.basicAck(deliveryTag, false)
                logger.error("payment_event outcome=dead_letter service=pix-boleto correlation_id={}", correlationId, exception)
                return
            }

            channel.basicReject(deliveryTag, false)
            logger.warn("payment_event outcome=retry service=pix-boleto correlation_id={}", correlationId, exception)
        } finally {
            MDC.remove("correlationId")
        }
    }

    private fun shouldDeadLetter(message: Message): Boolean {
        return RabbitRetrySupport.deadLettersFromQueue(
            message,
            RabbitPaymentConsumerTopology.BANK_RAIL_QUEUE,
        ) >= RabbitPaymentConsumerTopology.MAX_RETRY_COUNT
    }

    private fun safeCorrelationId(value: String): String? =
        value.takeIf { it.length in 1..128 && it.matches(Regex("[A-Za-z0-9._:-]+")) }
}
