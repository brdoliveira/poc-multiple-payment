CREATE TABLE bank_rail_charges (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(120) NOT NULL UNIQUE,
    rail VARCHAR(30) NOT NULL,
    provider VARCHAR(60) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    external_reference VARCHAR(180),
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bank_rail_charges_provider_status
    ON bank_rail_charges (provider, status);
