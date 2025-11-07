--liquibase formatted sql

--changeset radeflex:1
CREATE TABLE IF NOT EXISTS product(
    id SERIAL PRIMARY KEY,
    title VARCHAR(30) NOT NULL UNIQUE,
    description TEXT NOT NULL UNIQUE,
    price INTEGER NOT NULL,
    preview_url VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS users(
    id SERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    password VARCHAR(63) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
            CHECK (role IN('USER', 'ADMIN')),
    points INTEGER DEFAULT 0,
    balance INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS account(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(63) NOT NULL,
    email VARCHAR(100) NOT NULL,
    email_password VARCHAR(63) NOT NULL,
    product_id INT REFERENCES product NOT NULL
);

CREATE TABLE IF NOT EXISTS user_product(
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users,
    product_id INT REFERENCES product,
    quantity INT DEFAULT 1
);