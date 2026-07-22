package com.acme.payments.orchestrator.api;

import com.acme.payments.orchestrator.application.CreatePaymentCommand;
import com.acme.payments.orchestrator.application.PaymentOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class PaymentController {
    private final PaymentOrchestratorService paymentOrchestratorService;

    public PaymentController(PaymentOrchestratorService paymentOrchestratorService) {
        this.paymentOrchestratorService = paymentOrchestratorService;
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return PaymentResponse.from(paymentOrchestratorService.create(new CreatePaymentCommand(
                request.idempotencyKey(),
                request.method(),
                request.amount(),
                request.currency()
        )));
    }

    @GetMapping("/payments/{paymentId}")
    public PaymentResponse get(@PathVariable UUID paymentId) {
        return paymentOrchestratorService.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }
}
