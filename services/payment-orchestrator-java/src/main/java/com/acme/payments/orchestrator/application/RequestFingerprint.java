package com.acme.payments.orchestrator.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class RequestFingerprint {
    private RequestFingerprint() {
    }

    public static String of(CreatePaymentCommand command) {
        String canonical = String.join("|",
                command.method().name(),
                command.amount().stripTrailingZeros().toPlainString(),
                command.currency().toUpperCase(),
                value(command.preferredProvider()),
                value(command.installments()),
                hashSensitive(value(command.cardToken())),
                value(command.dueDate()),
                "payment-request-v1"
        );

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String hashSensitive(String value) {
        if (value.isBlank()) return "";
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                hex.append(String.format("%02x", item));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
