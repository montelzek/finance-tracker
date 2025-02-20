CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120),
    account_type VARCHAR(60),
    balance DOUBLE PRECISION,
    currency VARCHAR(30),
    created_at TIMESTAMP,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)