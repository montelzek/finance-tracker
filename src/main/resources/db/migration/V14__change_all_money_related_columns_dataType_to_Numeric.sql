ALTER TABLE accounts
ALTER COLUMN balance TYPE NUMERIC(15, 2)
USING ROUND(balance::NUMERIC, 2);

ALTER TABLE transactions
ALTER COLUMN amount TYPE NUMERIC(15, 2)
USING ROUND(amount::NUMERIC, 2);

ALTER TABLE budgets
ALTER COLUMN budget_size TYPE NUMERIC(15, 2)
USING ROUND(budget_size::NUMERIC, 2);

ALTER TABLE budgets
ALTER COLUMN budget_spent TYPE NUMERIC(15, 2)
USING ROUND(budget_spent::NUMERIC, 2);

ALTER TABLE financial_goals
ALTER COLUMN target_amount TYPE NUMERIC(15, 2)
USING ROUND(target_amount::NUMERIC, 2);

ALTER TABLE financial_goals
ALTER COLUMN current_amount TYPE NUMERIC(15, 2)
USING ROUND(current_amount::NUMERIC, 2);