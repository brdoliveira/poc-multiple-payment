package com.acme.payments.pixboleto.api

import com.acme.payments.pixboleto.domain.ChargeCommand
import com.acme.payments.pixboleto.domain.ChargeResult
import com.acme.payments.pixboleto.application.ChargeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class PixBoletoController(
    private val chargeService: ChargeService,
) {
    @PostMapping("/bank-rail/charges")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(@Valid @RequestBody request: ChargeRequest): ChargeResult {
        val command = ChargeCommand(
            idempotencyKey = request.idempotencyKey,
            rail = request.rail,
            amount = request.amount,
            currency = request.currency.uppercase(),
            dueDate = request.dueDate,
            preferredProvider = request.preferredProvider,
        )

        return chargeService.create(command)
    }
}
