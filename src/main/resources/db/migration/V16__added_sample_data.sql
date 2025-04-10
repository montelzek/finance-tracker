INSERT INTO users (id, email, password, first_name, last_name, created_at) VALUES
(nextval('users_id_seq'), 'admin@financetracker.com', '$2a$12$oiKeIVw.0oBbqeYH7v4J2eInRfsG/LXtO6/guu.OK2/XQkngjl.ne', 'Jordan', 'Belfort', NOW()),
(nextval('users_id_seq'), 'user@financetracker.com', '$2a$12$QbQ5cUPNmA93AiRxAGz91ux0KJ41xoDwyJl4ZTyzVBjaAHLKGBq8C', 'Jane', 'Doe', NOW());

INSERT INTO user_roles (user_id, role_id) VALUES (1, 3);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1);

INSERT INTO accounts (id, name, account_type, balance, currency, created_at, user_id) VALUES
(nextval('accounts_id_seq'), 'Main Checking Account', 'CHECKING', 5250.75, 'USD', NOW() - INTERVAL '7 month', 2),
(nextval('accounts_id_seq'), 'Savings Fund', 'SAVINGS', 15800.00, 'USD', NOW() - INTERVAL '6 month', 2),
(nextval('accounts_id_seq'), 'Everyday Card', 'CREDIT_CARD', -450.20, 'USD', NOW() - INTERVAL '5 month', 2),
(nextval('accounts_id_seq'), 'Holiday Savings EUR', 'SAVINGS', 2100.50, 'EUR', NOW() - INTERVAL '4 month', 2),
(nextval('accounts_id_seq'), 'Cash Wallet', 'CASH', 150.00, 'PLN', NOW() - INTERVAL '3 month', 2);

INSERT INTO financial_goals (id, name, target_amount, current_amount, is_achieved, created_at, user_id) VALUES
(nextval('financial_goals_id_seq'), 'iPhone', 1200.00, 500.00, false, NOW() - INTERVAL '5 month', 2),
(nextval('financial_goals_id_seq'), 'Emergency Fund', 5000.00, 4000.00, false, NOW() - INTERVAL '4 month', 2),
(nextval('financial_goals_id_seq'), 'Vacation Trip', 3000.00, 500.00, false, NOW() - INTERVAL '3 month', 2);

INSERT INTO budgets (id, name, start_date, end_date, budget_size, budget_spent, created_at, user_id, category_id) VALUES
(nextval('budgets_id_seq'), 'March Groceries', '2025-03-01', '2025-03-31', 400.00, 390.00, NOW() - INTERVAL '1 month', 2, 6), -- Groceries (ID 6)
(nextval('budgets_id_seq'), 'February Dining Out', '2025-04-01', '2025-04-30', 250.00, 260.00, NOW() - INTERVAL '1 month', 2, 11), -- Dining Out (ID 11)
(nextval('budgets_id_seq'), 'Entertainment', '2025-04-9', '2025-05-20', 700.00, 20.00, NOW() - INTERVAL '2 month', 2, 10), -- Entertainment (ID 10)
(nextval('budgets_id_seq'), 'Q1 Transportation', '2025-04-01', '2025-05-31', 300.00, 0.00, NOW() - INTERVAL '3 month', 2, 9); -- Transportation (ID 9)



INSERT INTO transactions (account_id, amount, date, description, category_id, created_at, financial_goal_id, budget_id) VALUES
(1, 4500.00, '2025-03-01', 'March Salary', 1, NOW(), NULL, NULL),
(3, 85.50, '2025-03-02', 'Grocery Store Run', 6, NOW(), NULL, 1),
(3, 55.20, '2025-03-05', 'Dinner with friends', 11, NOW(), NULL, 2),
(1, 1200.00, '2025-03-05', 'Rent Payment', 7, NOW(), NULL, NULL),
(1, 75.00, '2025-03-07', 'Electricity Bill', 8, NOW(), NULL, NULL),
(5, 40.00, '2025-03-10', 'Coffee shop cash', 11, NOW(), NULL, 2),
(3, 110.30, '2025-03-12', 'Weekly Groceries', 6, NOW(), NULL, 1),
(1, 50.00, '2025-03-15', 'Gasoline Top-up', 9, NOW(), NULL, 4),
(2, 500.00, '2025-03-18', 'Contribution to Car Fund', 15, NOW(), 1, NULL),
(3, 65.00, '2025-03-20', 'Movie Tickets', 10, NOW(), NULL, NULL),
(3, 95.80, '2025-03-22', 'Shopping - Clothes', 14, NOW(), NULL, NULL),
(3, 130.15, '2025-03-25', 'Supermarket', 6, NOW(), NULL, 1),
(2, 250.00, '2025-03-28', 'Saving for Emergency Fund', 15, NOW(), 2, NULL),
(4, 150.00, '2025-03-30', 'Weekend Trip Food (EUR)', 11, NOW(), NULL, 2),
(1, 4500.00, '2025-04-01', 'April Salary', 1, NOW(), NULL, NULL);

INSERT INTO transactions (account_id, amount, date, description, category_id, created_at, financial_goal_id, budget_id) VALUES
(1, 4500.00, '2025-02-01', 'February Salary', 1, NOW() - INTERVAL '1 month', NULL, NULL),
(3, 92.10, '2025-02-03', 'Groceries', 6, NOW() - INTERVAL '1 month', NULL, NULL),
(1, 1200.00, '2025-02-05', 'Rent Payment', 7, NOW() - INTERVAL '1 month', NULL, NULL),
(1, 80.00, '2025-02-07', 'Utilities Bill', 8, NOW() - INTERVAL '1 month', NULL, NULL),
(3, 45.00, '2025-02-10', 'Lunch Out', 11, NOW() - INTERVAL '1 month', NULL, NULL),
(1, 60.00, '2025-02-12', 'Public Transport Pass', 9, NOW() - INTERVAL '1 month', NULL, 4),
(3, 105.00, '2025-02-14', 'Valentine Dinner', 11, NOW() - INTERVAL '1 month', NULL, NULL),
(2, 500.00, '2025-02-15', 'Car Fund Savings', 15, NOW() - INTERVAL '1 month', 1, NULL),
(3, 70.00, '2025-02-18', 'Concert Ticket', 10, NOW() - INTERVAL '1 month', NULL, 3),
(3, 120.50, '2025-02-20', 'Grocery Haul', 6, NOW() - INTERVAL '1 month', NULL, NULL),
(4, 100.00, '2025-02-22', 'Bookstore (EUR)', 14, NOW() - INTERVAL '1 month', NULL, NULL),
(2, 200.00, '2025-02-25', 'Emergency Fund Top-up', 15, NOW() - INTERVAL '1 month', 2, NULL),
(5, 60.00, '2025-02-27', 'Snacks Cash (PLN)', 6, NOW() - INTERVAL '1 month', NULL, NULL);

INSERT INTO transactions (account_id, amount, date, description, category_id, created_at, financial_goal_id, budget_id) VALUES
(1, 4500.00, '2025-01-01', 'January Salary', 1, NOW() - INTERVAL '2 months', NULL, NULL),
(1, 500.00, '2025-01-02', 'Freelance Project Payment', 2, NOW() - INTERVAL '2 months', NULL, NULL),
(3, 88.75, '2025-01-04', 'Groceries', 6, NOW() - INTERVAL '2 months', NULL, NULL),
(1, 1200.00, '2025-01-05', 'Rent Payment', 7, NOW() - INTERVAL '2 months', NULL, NULL),
(1, 78.50, '2025-01-07', 'Gas & Water Bill', 8, NOW() - INTERVAL '2 months', NULL, NULL),
(3, 62.00, '2025-01-10', 'Restaurant Lunch', 11, NOW() - INTERVAL '2 months', NULL, NULL),
(1, 55.00, '2025-01-13', 'Train Tickets', 9, NOW() - INTERVAL '2 months', NULL, 4),
(2, 500.00, '2025-01-15', 'Save for Car', 15, NOW() - INTERVAL '2 months', 1, NULL),
(3, 150.00, '2025-01-18', 'New Jacket', 14, NOW() - INTERVAL '2 months', NULL, NULL),
(3, 115.40, '2025-01-21', 'Groceries', 6, NOW() - INTERVAL '2 months', NULL, NULL),
(1, 200.00, '2025-01-24', 'Health Insurance Premium', 13, NOW() - INTERVAL '2 months', NULL, NULL),
(2, 150.00, '2025-01-27', 'Vacation Fund Deposit', 15, NOW() - INTERVAL '2 months', 3, NULL),
(4, 80.00, '2025-01-30', 'Museum Tickets (EUR)', 10, NOW() - INTERVAL '2 months', NULL, NULL);

INSERT INTO transactions (account_id, amount, date, description, category_id, created_at, financial_goal_id, budget_id) VALUES
(1, 4400.00, '2024-12-01', 'December Salary', 1, NOW() - INTERVAL '3 months', NULL, NULL),
(3, 150.60, '2024-12-03', 'Holiday Groceries', 6, NOW() - INTERVAL '3 months', NULL, NULL),
(1, 1200.00, '2024-12-05', 'Rent Payment', 7, NOW() - INTERVAL '3 months', NULL, NULL),
(1, 95.00, '2024-12-07', 'Heating Bill', 8, NOW() - INTERVAL '3 months', NULL, NULL),
(3, 250.00, '2024-12-10', 'Holiday Gifts Shopping', 14, NOW() - INTERVAL '3 months', NULL, NULL),
(1, 40.00, '2024-12-12', 'Bus Fare', 9, NOW() - INTERVAL '3 months', NULL, NULL),
(3, 80.00, '2024-12-15', 'Holiday Party Dinner', 11, NOW() - INTERVAL '3 months', NULL, NULL),
(2, 400.00, '2024-12-18', 'Car Savings', 15, NOW() - INTERVAL '3 months', 1, NULL), -- Goal 1
(3, 95.00, '2024-12-20', 'Pharmacy', 12, NOW() - INTERVAL '3 months', NULL, NULL),
(3, 100.20, '2024-12-22', 'Groceries', 6, NOW() - INTERVAL '3 months', NULL, NULL),
(5, 100.00, '2024-12-24', 'Last minute gifts cash (PLN)', 14, NOW() - INTERVAL '3 months', NULL, NULL),
(4, 50.00, '2024-12-28', 'Post-Holiday Coffee (EUR)', 11, NOW() - INTERVAL '3 months', NULL, NULL),
(2, 100.00, '2024-12-30', 'Saving for Vacation', 15, NOW() - INTERVAL '3 months', 3, NULL); -- Goal 3

INSERT INTO transactions (account_id, amount, date, description, category_id, created_at, financial_goal_id, budget_id) VALUES
(1, 4400.00, '2024-11-01', 'November Salary', 1, NOW() - INTERVAL '4 months', NULL, NULL),
(3, 98.25, '2024-11-03', 'Groceries', 6, NOW() - INTERVAL '4 months', NULL, NULL),
(1, 1200.00, '2024-11-05', 'Rent Payment', 7, NOW() - INTERVAL '4 months', NULL, NULL),
(1, 70.00, '2024-11-07', 'Internet Bill', 8, NOW() - INTERVAL '4 months', NULL, NULL),
(3, 50.00, '2024-11-10', 'Takeaway Pizza', 11, NOW() - INTERVAL '4 months', NULL, NULL),
(1, 45.00, '2024-11-13', 'Gas', 9, NOW() - INTERVAL '4 months', NULL, NULL),
(2, 400.00, '2024-11-15', 'Saving for Car', 15, NOW() - INTERVAL '4 months', 1, NULL), -- Goal 1
(3, 60.00, '2024-11-18', 'Cinema', 10, NOW() - INTERVAL '4 months', NULL, NULL),
(3, 110.00, '2024-11-21', 'Grocery Stock-up', 6, NOW() - INTERVAL '4 months', NULL, NULL),
(1, 200.00, '2024-11-24', 'Car Insurance', 13, NOW() - INTERVAL '4 months', NULL, NULL),
(2, 150.00, '2024-11-27', 'Emergency Fund', 15, NOW() - INTERVAL '4 months', 2, NULL), -- Goal 2
(4, 75.00, '2024-11-29', 'Dinner Out (EUR)', 11, NOW() - INTERVAL '4 months', NULL, NULL);


INSERT INTO transactions (account_id, amount, date, description, category_id, created_at, financial_goal_id, budget_id) VALUES
(1, 4400.00, '2024-10-01', 'October Salary', 1, NOW() - INTERVAL '5 months', NULL, NULL),
(1, 600.00, '2024-10-02', 'Side Project Income', 2, NOW() - INTERVAL '5 months', NULL, NULL),
(3, 105.80, '2024-10-04', 'Groceries', 6, NOW() - INTERVAL '5 months', NULL, NULL),
(1, 1200.00, '2024-10-05', 'Rent Payment', 7, NOW() - INTERVAL '5 months', NULL, NULL),
(1, 72.30, '2024-10-07', 'Utilities', 8, NOW() - INTERVAL '5 months', NULL, NULL),
(3, 68.00, '2024-10-10', 'Lunch Meeting', 11, NOW() - INTERVAL '5 months', NULL, NULL),
(1, 50.00, '2024-10-12', 'Metro Card Top-up', 9, NOW() - INTERVAL '5 months', NULL, NULL),
(2, 400.00, '2024-10-15', 'Saving towards Car', 15, NOW() - INTERVAL '5 months', 1, NULL), -- Goal 1
(3, 85.00, '2024-10-18', 'New Shoes', 14, NOW() - INTERVAL '5 months', NULL, NULL),
(3, 125.50, '2024-10-21', 'Supermarket Run', 6, NOW() - INTERVAL '5 months', NULL, NULL),
(1, 40.00, '2024-10-25', 'Doctor Co-pay', 12, NOW() - INTERVAL '5 months', NULL, NULL),
(2, 100.00, '2024-10-28', 'Vacation Savings', 15, NOW() - INTERVAL '5 months', 3, NULL), -- Goal 3
(5, 50.00, '2024-10-30', 'Quick Lunch Cash (PLN)', 11, NOW() - INTERVAL '5 months', NULL, NULL);