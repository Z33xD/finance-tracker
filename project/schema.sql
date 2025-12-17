-- Table Creations

CREATE TABLE "users" (
     "id" SERIAL,
     "username" VARCHAR(32) UNIQUE NOT NULL,
     "email" VARCHAR(64) UNIQUE NOT NULL,
     "password_hash" VARCHAR(256) NOT NULL,
     "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY ("id")
);

CREATE TABLE "accounts" (
    "id" SERIAL,
    "user_id" INT REFERENCES "users"("id") ON DELETE CASCADE,
    "account_name" VARCHAR(64) NOT NULL,
    "account_type" VARCHAR(32), -- savings, credit_card, etc.
    "currency" CHAR(3) DEFAULT 'INR',
    "initial_balance" DECIMAL(12, 2) DEFAULT 0,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);

CREATE TABLE "categories" (
    "id" SERIAL,
    "user_id" INT REFERENCES "users"("id") ON DELETE CASCADE,
    "name" VARCHAR(16) NOT NULL,
    "type" VARCHAR(16), -- income/expense, for example
    "icon" CHAR(1),
    "colour" VARCHAR(7), -- to be stored as hex-code
    PRIMARY KEY ("id")
);

CREATE TABLE "transactions" (
    "id" SERIAL,
    "account_id" INT REFERENCES "accounts"("id") ON DELETE CASCADE,
    "category_id" INT REFERENCES "categories"("id"),
    "amount" DECIMAL(12, 2) NOT NULL,
    "transaction_rate" DATE NOT NULL,
    "description" VARCHAR(256),
    "transaction_type" VARCHAR(16), -- debit or credit for example
    "import_batch_id" INT,
    "datetime" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);

CREATE TABLE "budgets" (
    "id" SERIAL,
    "user_id" INT REFERENCES "users"("id") ON DELETE CASCADE,
    "category_id" INT REFERENCES "categories"("id"),
    "amount" DECIMAL(12, 2) NOT NULL,
    "month" INT NOT NULL CHECK ( month BETWEEN 1 AND 12 ),
    "year" INT NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    UNIQUE ("user_id", "category_id", "month", "year")
);

CREATE TABLE "import_batches" (
    "id" SERIAL,
    "user_id" INT REFERENCES "users"("id") ON DELETE CASCADE,
    "file_name" VARCHAR(256),
    "import_type" VARCHAR(8), -- for example, csv, pdf, api
    "status" VARCHAR(128), -- for example, pending, processing, completed, failed, etc.
    "total_records" INT DEFAULT 0,
    "successful_records" INT DEFAULT 0,
    "failed_records" INT DEFAULT 0,
    "started_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "completed_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "error_message" TEXT,
    PRIMARY KEY ("id")
);

CREATE TABLE "exchange_rates" (
    "id" SERIAL,
    "from_currency" CHAR(3) NOT NULL,
    "to_currency" CHAR(3) NOT NULL,
    "rate" DECIMAL (10, 6) NOT NULL,
    "date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    UNIQUE("from_currency", "to_currency", "date")
);