--liquibase formatted sql

--changeset radeflex:3
ALTER TABLE account
    ADD COLUMN IF NOT EXISTS available BOOLEAN NOT NULL DEFAULT true;