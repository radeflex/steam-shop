--liquibase formatted sql

--changeset radeflex:5
CREATE TABLE IF NOT EXISTS notification(
    id SERIAL PRIMARY KEY,
    title VARCHAR(30) NOT NULL,
    text TEXT NOT NULL,
    type VARCHAR(15) NOT NULL,
    user_id INT REFERENCES users ON DELETE CASCADE,
    created_by INT REFERENCES users ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notification_read(
    id SERIAL PRIMARY KEY,
    notification_id INT REFERENCES notification ON DELETE CASCADE,
    user_id INT REFERENCES users ON DELETE CASCADE
);