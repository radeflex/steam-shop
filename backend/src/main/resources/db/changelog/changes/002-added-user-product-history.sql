--liquibase formatted sql

--changeset radeflex:2
CREATE TABLE IF NOT EXISTS user_product_history(
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users ON DELETE CASCADE,
    product_id INT REFERENCES product ON DELETE SET NULL,
    title VARCHAR(30) NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

