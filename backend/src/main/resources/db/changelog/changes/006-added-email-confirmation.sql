--liquibase formatted sql

--changeset radeflex:6
CREATE TABLE IF NOT EXISTS email_confirmation(
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users ON DELETE CASCADE,
    token UUID NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE users ADD COLUMN confirmed BOOLEAN NOT NULL DEFAULT false;