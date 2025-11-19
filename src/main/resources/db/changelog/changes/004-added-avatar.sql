--liquibase formatted sql

--changeset radeflex:4
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(200);