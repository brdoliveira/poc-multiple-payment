package com.acme.payments.pixboleto.infrastructure

import com.acme.payments.pixboleto.application.ChargeRepository
import com.acme.payments.pixboleto.application.RequestFingerprint
import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.domain.PaymentRail
import com.acme.payments.pixboleto.provider.PaymentProvider
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

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

    override fun findFingerprint(idempotencyKey: String): String? {
        return jdbcTemplate.query(
            "SELECT request_fingerprint FROM bank_rail_charges WHERE idempotency_key = ?",
            { rs, _ -> rs.getString("request_fingerprint") },
            idempotencyKey,
        ).firstOrNull()
    }

    override fun reserve(
        command: ChargeCommand,
        provider: PaymentProvider,
        chargeId: UUID,
        requestFingerprint: String,
    ): Boolean {
        return jdbcTemplate.update(
            """
            INSERT INTO bank_rail_charges (
                id, idempotency_key, rail, provider, amount, currency, status,
                external_reference, due_date, request_fingerprint, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, 'IN_PROGRESS', ?, ?, ?, NOW(), NOW())
            ON CONFLICT (idempotency_key) DO NOTHING
            """.trimIndent(),
            chargeId,
            command.idempotencyKey,
            command.rail.name,
            provider.name,
            command.amount,
            command.currency.uppercase(),
            "pending:$chargeId",
            command.dueDate,
            requestFingerprint,
        ) == 1
    }

    override fun save(command: ChargeCommand, result: ChargeResult): ChargeResult {
        jdbcTemplate.update(
            """
            UPDATE bank_rail_charges
            SET provider = ?, amount = ?, currency = ?, status = ?, external_reference = ?, updated_at = NOW()
            WHERE idempotency_key = ? AND request_fingerprint = ?
            """.trimIndent(),
            result.provider.name,
            command.amount,
            command.currency.uppercase(),
            result.status,
            result.externalReference,
            command.idempotencyKey,
            RequestFingerprint.of(command),
        )
        return findByIdempotencyKey(command.idempotencyKey) ?: result
    }
}
