-- Users

-- 1. GET /api/users
SELECT "id", "username", "email", "password_hash", "created_at"
FROM "users";

-- 2. GET /api/users?id={id}
SELECT "id", "username", "email", "password_hash", "created_at"
FROM "users"
WHERE id = ?;

-- 3. GET /api/users?name={name}
SELECT "id", "username", "email", "password_hash", "created_at"
FROM "users"
WHERE "username" = ?;

-- 3. GET /api/users?email={email}
SELECT "id", "username", "email", "password_hash", "created_at"
FROM "users"
WHERE "email" = ?;

-- 4. POST /api/users
INSERT INTO users (username, email, password_hash)
VALUES (?, ?, ?);

-- 5. PUT /api/users/{id}
UPDATE users
SET
    username = ?,
    email = ?,
    password_hash = ?
WHERE id = ?;

-- 6. DELETE /api/users/{id}
DELETE FROM users
WHERE id = ?;

-----

-- Accounts

-- 1. GET /api/accounts
SELECT "id", "user_id", "account_name", "account_type", "currency", "initial_balance", "created_at"
FROM "accounts";

-- 2. GET /api/accounts?id={id}
SELECT "id", "user_id", "account_name", "account_type", "currency", "initial_balance", "created_at"
FROM "accounts"
WHERE "id" = ?;

-- 3. GET /api/accounts?user_id={user_id}
SELECT "id", "user_id", "account_name", "account_type", "currency", "initial_balance", "created_at"
FROM "accounts"
WHERE "user_id" = ?;

-- 4. GET /api/accounts?account_type={account_type}
SELECT "id", "user_id", "account_name", "account_type", "currency", "initial_balance", "created_at"
FROM "accounts"
WHERE "account_type" = ?;

-- 5. GET /api/accounts/{id}
SELECT "id", "user_id", "account_name", "account_type", "currency", "initial_balance", "created_at"
FROM "accounts"
WHERE "id" = ?;

-- 6. POST /api/accounts
INSERT INTO "accounts" ("user_id", "account_name", "account_type", "currency", "initial_balance")
VALUES (?, ?, ?, ?, ?);

-- 7. PUT /api/accounts/{id}
UPDATE "accounts"
SET
    "user_id" = ?,
    "account_name" = ?,
    "account_type" = ?,
    "currency" = ?,
    "initial_balance" = ?
WHERE "id" = ?;

-- 8. DELETE /api/accounts/{id}
DELETE FROM "accounts"
WHERE "id" = ?;

-----

-- Categories

-- 1. GET /api/categories
SELECT "id", "user_id", "name", "type", "icon", "colour"
FROM "categories";

-- 2. GET /api/categories?id={id}
SELECT "id", "user_id", "name", "type", "icon", "colour"
FROM "categories"
WHERE "id" = ?;

-- 3. GET /api/categories?name={name}
SELECT "id", "user_id", "name", "type", "icon", "colour"
FROM "categories"
WHERE "name" = ?;

-- 4. GET /api/categories/{id}
SELECT "id", "user_id", "name", "type", "icon", "colour"
FROM "categories"
WHERE "id" = ?;

-- 5. POST /api/categories
INSERT INTO "categories" ("user_id", "name", "type", "icon", "colour")
VALUES (?, ?, ?, ?, ?);

-- 6. POST /api/categories/{id}
UPDATE "categories"
SET
    "user_id" = ?,
    "name" = ?,
    "type" = ?,
    "icon" = ?,
    "colour" = ?
WHERE "id" = ?;

-- 7. DELETE /api/categories/{id}
DELETE FROM "categories"
WHERE "id" = ?;

-----

-- Transactions

-- 1. GET /api/transactions
SELECT "id", "account_id", "category_id", "amount", "transaction_date", "description", "transaction_type", "import_batch_id", "datetime"
FROM "transactions";

-- 2. GET /api/transactions?id={id}
SELECT "id", "account_id", "category_id", "amount", "transaction_date", "description", "transaction_type", "import_batch_id", "datetime"
FROM "transactions"
WHERE "id" = ?;

-- 3. GET /api/transactions?date={date}
SELECT "id", "account_id", "category_id", "amount", "transaction_date", "description", "transaction_type", "import_batch_id", "datetime"
FROM "transactions"
WHERE "transaction_date" = ?;

-- 4. GET /api/transactions/{id}
SELECT "id", "account_id", "category_id", "amount", "transaction_date", "description", "transaction_type", "import_batch_id", "datetime"
FROM "transactions"
WHERE "id" = ?;

-- 5. POST /api/transactions
INSERT INTO "transactions" ("account_id", "category_id", "amount", "transaction_date", "description", "transaction_type", "import_batch_id")
VALUES (?, ?, ?, ?, ?, ?, ?);

-- 6. PUT /api/transactions/{id}
UPDATE "transactions"
SET
    "account_id" = ?,
    "category_id" = ?,
    "amount" = ?,
    "transaction_date" = ?,
    "description" = ?,
    "transaction_type" = ?,
    "import_batch_id" = ?
WHERE "id" = ?;

-- 7. DELETE /api/transactions/{id}
DELETE FROM "transactions"
WHERE "id" = ?;

-- 8. GET /api/transactions/search
SELECT "id", "account_id", "category_id", "amount", "transaction_date", "description", "transaction_type", "import_batch_id", "datetime"
FROM "transactions"
WHERE ( ? IS NULL OR "category_id" = ? )
AND ( ? IS NULL OR "transaction_date" >= ? )
AND ( ? IS NULL OR "transaction_date" <= ? )
AND ( ? IS NULL OR "transaction_type" = ? );

-----

-- Import Batches

-- 1. GET /api/import-batches
SELECT "id", "user_id", "file_name", "import_type", "status", "total_records", "successful_records", "failed_records", "started_at", "completed_at", "error_message"
FROM "import_batches";

-- 2. GET /api/import-batches?id={id}
SELECT "id", "user_id", "file_name", "import_type", "status", "total_records", "successful_records", "failed_records", "started_at", "completed_at", "error_message"
FROM "import_batches"
WHERE "id" = ?;

-- 3. GET /api/import-batches?fileName={fileName}
SELECT "id", "user_id", "file_name", "import_type", "status", "total_records", "successful_records", "failed_records", "started_at", "completed_at", "error_message"
FROM "import_batches"
WHERE "file_name" = ?;

-- 4. GET /api/import-batches?status={status}
SELECT "id", "user_id", "file_name", "import_type", "status", "total_records", "successful_records", "failed_records", "started_at", "completed_at", "error_message"
FROM "import_batches"
WHERE "status" = ?;

-- 5. GET /api/import-batches/{id}
SELECT "id", "user_id", "file_name", "import_type", "status", "total_records", "successful_records", "failed_records", "started_at", "completed_at", "error_message"
FROM "import_batches"
WHERE "id" = ?;

-- 6. POST /api/import-batches/upload (Dummy example)
INSERT INTO "import_batches" ("user_id", "file_name", "import_type", "status", "total_records", "successful_records", "failed_records")
VALUES (?, ?, 'csv', ?, 0, 0, 0);

-- 7. DELETE /api/import-batches/{id}
DELETE FROM "import_batches"
WHERE "id" = ?;

-----

-- Exchange Rates

-- 1. GET /api/exchange-rates
SELECT "id", "from_currency", "to_currency", "rate", "date", "created_at"
FROM "exchange_rates";

-- 2. GET /api/exchange-rates?id={id}
SELECT "id", "from_currency", "to_currency", "rate", "date", "created_at"
FROM "exchange_rates"
WHERE "id" = ?;

-- 3. GET /api/exchange-rates?from_currency={from}&to_currency={to}&date={date}
SELECT "id", "from_currency", "to_currency", "rate", "date", "created_at"
FROM "exchange_rates"
WHERE "from_currency" = ?
AND "to_currency" = ?
AND "date" = ?;

-- 4. GET /api/exchange-rates?date={date}
SELECT "id", "from_currency", "to_currency", "rate", "date", "created_at"
FROM "exchange_rates"
WHERE "date" = ?;

-- 5. GET /api/exchange-rates/{baseCurrency}/{targetCurrency} (today)
SELECT "id", "from_currency", "to_currency", "rate", "date", "created_at"
FROM "exchange_rates"
WHERE "from_currency" = ?
AND "to_currency" = ?
AND "date" = CURRENT_DATE;

-- 6. GET /api/exchange-rates/{baseCurrency}/{targetCurrency}/{date}
SELECT "id", "from_currency", "to_currency", "rate", "date", "created_at"
FROM "exchange_rates"
WHERE "from_currency" = ?
AND "to_currency" = ?
AND "date" = ?;

-- 7. POST /api/exchange-rates
INSERT INTO "exchange_rates" ("from_currency", "to_currency", "rate", "date")
VALUES (?, ?, ?, ?);

-- 8. PUT /api/exchange-rates/{id}
UPDATE "exchange_rates"
SET
    "from_currency" = ?,
    "to_currency" = ?,
    "rate" = ?,
    "date" = ?
WHERE "id" = ?;

-- 9. DELETE /api/exchange-rates/{id}
DELETE FROM "exchange_rates"
WHERE "id" = ?;

-- 10. POST /api/exchange-rates/refresh
INSERT INTO "exchange_rates" ("from_currency", "to_currency", "rate", "date")
VALUES (?, ?, ?, CURRENT_DATE);