CREATE TABLE card_payments (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(120) NOT NULL UNIQUE,
    provider VARCHAR(60) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    installments INTEGER NOT NULL CHECK (installments > 0),
    status VARCHAR(40) NOT NULL,
    external_authorization_id VARCHAR(180) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE card_authorization_attempts (
    id UUID PRIMARY KEY,
    card_payment_id UUID NOT NULL REFERENCES card_payments(id),
    provider VARCHAR(60) NOT NULL,
    status VARCHAR(40) NOT NULL,
    error_code VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_card_authorization_attempts_payment
    ON card_authorization_attempts (card_payment_id, created_at);
