CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE payment ADD COLUMN idempotency_key UUID;

UPDATE payment
SET idempotency_key = gen_random_uuid()
WHERE idempotency_key IS NULL;

ALTER TABLE payment
    ALTER COLUMN idempotency_key SET NOT NULL;

ALTER TABLE payment
    ADD CONSTRAINT payment_idempotency_key_uniq UNIQUE (idempotency_key);