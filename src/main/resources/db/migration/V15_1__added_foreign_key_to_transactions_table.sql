ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_financial_goals
FOREIGN KEY (financial_goal_id)
REFERENCES financial_goals(id)
ON DELETE SET NULL