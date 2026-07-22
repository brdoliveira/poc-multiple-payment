package com.acme.payments.pixboleto.infrastructure

import com.acme.payments.pixboleto.application.ChargeRepository
import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.domain.PaymentRail
import com.acme.payments.pixboleto.provider.PaymentProvider
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class JdbcChargeRepository(
    private val jdbcTemplate: JdbcTemplate,
) : ChargeRepository {
    override fun findByIdempotencyKey(idempotencyKey: String): ChargeResult? {
        return jdbcTemplate.query(
            """
            SELECT id, rail, provider, status, external_reference
            FROM bank_rail_charges
            WHERE idempotency_key = ?
            """.trimIndent(),
            { rs, _ ->
                ChargeResult(
                    chargeId = rs.getObject("id", java.util.UUID::class.java),
                    provider = PaymentProvider.valueOf(rs.getString("provider")),
                    rail = PaymentRail.valueOf(rs.getString("rail")),
                    status = rs.getString("status"),
                    externalReference = rs.getString("external_reference"),
                    customerAction = null,
                )
            },
            idempotencyKey,
        ).firstOrNull()
    }

    override fun save(command: ChargeCommand, result: ChargeResult): ChargeResult {
        jdbcTemplate.update(
            """
            INSERT INTO bank_rail_charges (
                id, idempotency_key, rail, provider, amount, currency, status,
                external_reference, due_date, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            ON CONFLICT (idempotency_key) DO NOTHING
            """.trimIndent(),
            result.chargeId,
            command.idempotencyKey,
            result.rail.name,
            result.provider.name,
            command.amount,
            command.currency.uppercase(),
            result.status,
            result.externalReference,
            command.dueDate,
        )
        return findByIdempotencyKey(command.idempotencyKey) ?: result
    }
}
