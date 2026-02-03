--liquibase formatted sql

--changeset radeflex:7
CREATE TABLE IF NOT EXISTS payment(
    id UUID PRIMARY KEY,
    order_id SERIAL UNIQUE,
    amount NUMERIC(12, 2) NOT NULL,
    user_id INT REFERENCES users ON DELETE SET NULL,
    confirmation_url VARCHAR(500) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS payment_item(
    id SERIAL PRIMARY KEY,
    payment_id UUID REFERENCES payment ON DELETE CASCADE,
    product_id INT REFERENCES product ON DELETE SET NULL,
    quantity INT NOT NULL
);

ALTER TABLE user_product_history ADD COLUMN payment_id UUID REFERENCES payment;
ALTER TABLE notification ADD COLUMN payment_id UUID REFERENCES payment ON DELETE SET NULL;
ALTER TABLE notification ADD COLUMN payment_status VARCHAR(20);
ALTER TABLE account DROP COLUMN available;
ALTER TABLE account ADD COLUMN status VARCHAR(20) NOT NULL;
ALTER TABLE notification ALTER COLUMN title TYPE VARCHAR(60);