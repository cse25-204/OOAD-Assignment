-- SM BANKING database schema (SQLite). Created automatically by DatabaseManager.initSchema().

CREATE TABLE IF NOT EXISTS customers(
  customer_id TEXT PRIMARY KEY,
  customer_type TEXT NOT NULL CHECK (customer_type IN ('INDIVIDUAL','COMPANY')),
  first_name TEXT,
  surname TEXT,
  id_number TEXT,
  company_name TEXT,
  registration_number TEXT,
  address TEXT NOT NULL,
  phone TEXT NOT NULL,
  email TEXT NOT NULL);

CREATE TABLE IF NOT EXISTS accounts(
  account_number TEXT PRIMARY KEY,
  account_type TEXT NOT NULL CHECK (account_type IN ('SAVINGS','INVESTMENT','CHEQUE')),
  customer_id TEXT NOT NULL REFERENCES customers(customer_id) ON DELETE RESTRICT,
  branch TEXT NOT NULL,
  balance REAL NOT NULL CHECK (balance >= 0),
  date_opened TEXT NOT NULL,
  employer_name TEXT,
  employer_address TEXT);

CREATE TABLE IF NOT EXISTS transactions(
  transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
  account_number TEXT NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,
  tx_type TEXT NOT NULL CHECK (tx_type IN ('DEPOSIT','WITHDRAWAL','INTEREST')),
  amount REAL NOT NULL CHECK (amount > 0),
  balance_after REAL NOT NULL,
  tx_timestamp TEXT NOT NULL,
  description TEXT);

CREATE TABLE IF NOT EXISTS users(
  username TEXT PRIMARY KEY,
  password_hash TEXT NOT NULL,
  salt TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('TELLER','CUSTOMER')),
  customer_id TEXT REFERENCES customers(customer_id) ON DELETE CASCADE);

CREATE INDEX IF NOT EXISTS idx_accounts_customer ON accounts(customer_id);

CREATE INDEX IF NOT EXISTS idx_tx_account ON transactions(account_number);
