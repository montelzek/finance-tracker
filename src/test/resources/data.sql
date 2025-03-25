INSERT INTO roles (name)
SELECT 'ROLE_USER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER');

INSERT INTO roles (name)
SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN');

INSERT INTO roles (name)
SELECT 'ROLE_PREMIUM' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_PREMIUM');

INSERT INTO categories (name, type) VALUES ('Salary', 'INCOME');
INSERT INTO categories (name, type) VALUES ('Freelance Work', 'INCOME');
INSERT INTO categories (name, type) VALUES ('Investment Returns', 'INCOME');
INSERT INTO categories (name, type) VALUES ('Gifts', 'INCOME');
INSERT INTO categories (name, type) VALUES ('Rental Income', 'INCOME');
INSERT INTO categories (name, type) VALUES ('Groceries', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Rent', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Utilities', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Transportation', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Entertainment', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Dining Out', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Healthcare', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Insurance', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Shopping', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Financial Goal', 'FINANCIAL_GOAL');
