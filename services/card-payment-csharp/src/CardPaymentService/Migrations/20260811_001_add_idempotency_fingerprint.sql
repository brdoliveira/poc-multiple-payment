ALTER TABLE card_payments
    ADD COLUMN IF NOT EXISTS request_fingerprint VARCHAR(64) NOT NULL DEFAULT 'legacy';
