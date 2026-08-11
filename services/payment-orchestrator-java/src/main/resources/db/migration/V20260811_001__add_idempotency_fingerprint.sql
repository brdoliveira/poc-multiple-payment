ALTER TABLE payment_idempotency_keys
    ADD COLUMN request_fingerprint VARCHAR(64) NOT NULL DEFAULT 'legacy';
