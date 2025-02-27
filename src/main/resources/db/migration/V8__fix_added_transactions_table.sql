CREATE TABLE transactions (
      id SERIAL PRIMARY KEY,
      account_id INT NOT NULL,
      amount DOUBLE PRECISION NOT NULL,
      date DATE NOT NULL,
      description VARCHAR(255),
      category_id INT NOT NULL,
      created_at TIMESTAMP NOT NULL,
      FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
      FOREIGN KEY (category_id) REFERENCES categories(id)
)