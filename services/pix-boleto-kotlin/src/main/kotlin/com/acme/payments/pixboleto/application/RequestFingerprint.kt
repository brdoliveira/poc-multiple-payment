package com.acme.payments.pixboleto.application

import com.acme.payments.pixboleto.domain.ChargeCommand
import java.security.MessageDigest

object RequestFingerprint {
    fun of(command: ChargeCommand): String {
        val canonical = listOf(
            command.rail.name,
            command.amount.stripTrailingZeros().toPlainString(),
            command.currency.uppercase(),
            command.dueDate?.toString().orEmpty(),
            command.preferredProvider?.name.orEmpty(),
        ).joinToString("|")
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
