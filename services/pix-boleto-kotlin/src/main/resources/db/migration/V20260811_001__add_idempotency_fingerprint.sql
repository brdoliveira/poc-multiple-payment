ALTER TABLE bank_rail_charges
    ADD COLUMN request_fingerprint VARCHAR(64) NOT NULL DEFAULT 'legacy';
