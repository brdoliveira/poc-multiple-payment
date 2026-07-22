CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE payment_idempotency_keys (
    key VARCHAR(120) PRIMARY KEY,
    payment_id UUID NOT NULL UNIQUE,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payment_idempotency_keys_expires
    ON payment_idempotency_keys (expires_at);

CREATE TABLE payment_reconciliation_jobs (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id),
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    next_run_at TIMESTAMPTZ NOT NULL,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_pending_payment_reconciliation_jobs
    ON payment_reconciliation_jobs (payment_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_payment_reconciliation_jobs_next_run
    ON payment_reconciliation_jobs (status, next_run_at);
