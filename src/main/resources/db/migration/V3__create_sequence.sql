CREATE SEQUENCE users_id_seq START 1;

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq');