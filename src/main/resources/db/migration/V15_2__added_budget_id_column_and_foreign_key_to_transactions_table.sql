ALTER TABLE transactions
ADD COLUMN budget_id INT DEFAULT NULL,
ADD CONSTRAINT fk_transactions_budgets
    FOREIGN KEY (budget_id)
    REFERENCES budgets (id)
    ON DELETE SET NULL;