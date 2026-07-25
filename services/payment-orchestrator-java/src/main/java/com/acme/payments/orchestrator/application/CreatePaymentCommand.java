package com.acme.payments.orchestrator.application;

import com.acme.payments.orchestrator.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public record CreatePaymentCommand(
        String idempotencyKey,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        String preferredProvider,
        Integer installments,
        String cardToken,
        LocalDate dueDate
) {
    public CreatePaymentCommand(String idempotencyKey, PaymentMethod method, BigDecimal amount, String currency) {
        this(idempotencyKey, method, amount, currency, null, null, null, null);
    }

    public Map<String, Object> metadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        putIfPresent(metadata, "preferredProvider", preferredProvider);
        putIfPresent(metadata, "installments", installments);
        putIfPresent(metadata, "cardToken", cardToken);
        putIfPresent(metadata, "dueDate", dueDate == null ? null : dueDate.toString());
        return metadata;
    }

    private static void putIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
