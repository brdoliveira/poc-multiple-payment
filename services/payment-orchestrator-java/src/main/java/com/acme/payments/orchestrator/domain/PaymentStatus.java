package com.acme.payments.orchestrator.domain;

public enum PaymentStatus {
    CREATED,
    PROCESSING,
    AUTHORIZED,
    PAID,
    FAILED,
    CANCELED,
    REFUNDED
}
