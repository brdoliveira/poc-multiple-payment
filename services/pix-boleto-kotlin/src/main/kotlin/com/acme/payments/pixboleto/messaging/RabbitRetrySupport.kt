package com.acme.payments.pixboleto.messaging

import org.springframework.amqp.core.Message

object RabbitRetrySupport {
    fun deadLettersFromQueue(message: Message, queue: String): Long {
        val deaths = message.messageProperties.headers["x-death"] as? List<*> ?: return 0
        return deaths
            .asSequence()
            .filterIsInstance<Map<*, *>>()
            .firstOrNull { it["queue"] == queue }
            ?.get("count")
            ?.let(::asLong)
            ?: 0
    }

    private fun asLong(value: Any): Long {
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Short -> value.toLong()
            is Byte -> value.toLong()
            else -> value.toString().toLongOrNull() ?: 0
        }
    }
}
