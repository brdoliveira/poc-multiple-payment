package com.acme.payments.orchestrator.api;

import com.acme.payments.orchestrator.application.PaymentReconciliationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ReconciliationController {
    private final PaymentReconciliationService reconciliationService;

    public ReconciliationController(PaymentReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/reconciliation/payments/{paymentId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ReconciliationResponse enqueue(@PathVariable UUID paymentId) {
        reconciliationService.enqueuePayment(paymentId);
        return new ReconciliationResponse(1);
    }

    @PostMapping("/reconciliation/stale-payments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ReconciliationResponse enqueueStale(@RequestParam(defaultValue = "15") int olderThanMinutes) {
        return new ReconciliationResponse(reconciliationService.enqueueStaleProcessingPayments(olderThanMinutes));
    }
}
