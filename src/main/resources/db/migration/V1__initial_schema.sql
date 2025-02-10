CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT email_unique UNIQUE (email)
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO roles (name)
VALUES
    ('ROLE_USER'),
    ('ROLE_PREMIUM'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;