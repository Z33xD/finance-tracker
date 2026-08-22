# Finance Tracker

A full-stack, secure personal finance management application featuring production-grade authentication, real-time currency conversions, and automated bulk data processing.

---

## Overview

This is a Spring Boot based application that [I](https://www.github.com/Z33xD) made. It began as a hands-on backend exploration of Spring Boot and PostgreSQL, and is evolving into a full-stack financial utility. This app exposes robust RESTful APIs secured via stateless token authentication, and features a frontend interface to help users track budgets, manage accounts, and parse transaction histories.

---

## Key Features

### Secure Authentication & User Management
- **JWT Authentication:** Stateless, token-based user sign-up and login architecture utilising Spring Security and JSON Web Tokens (JWT).
- **Email Verification:** Security validation workflow during sign-up to verify user authenticity before unlocking account scopes.
- **Contextual Security:** All financial domain controllers (/accounts, /transactions, /budgets) are guarded and automatically scoped to the currently authenticated user.

### Financial Domain Engine
- **Multi-Asset Tracking:** Complete CRUD management for user accounts, customisable expense/income categories, transactions, and localised budgets.
- **Smart Budgeting:** Set up thresholds per category and evaluate limits against live transaction metrics.

### Bulk Processing & Extensibility

- **CSV Bulk Imports:** High-performance transactional ingestion handling bulk data with custom duplicate detection, historical logging, and `import_batch` error metrics tracking.
- **Live Exchange Rates:** Dynamically handles currency conversions across assets using integration hooks with the external [ExchangeRate-API](https://www.exchangerate-api.com/).

### Multi-Currency Support
- Accounts and budgets are each denominated in their own currency (all ~160 ExchangeRate-API codes are supported).
- Amounts are always stored in their native currency; conversion happens only at read/summary time via stored `exchange_rates` pairs for the transaction's date.
- The reporting currency is derived from your first-created account, and `GET /api/accounts/summary` converts every account balance into it for the dashboard total.
- `GET /api/budgets/summary` computes actual spending converted into each budget's own currency.
- Cross-rates are derived through the reporting currency as an anchor; rates refresh on demand (cached once per day) or manually via `POST /api/exchange-rates/refresh`.

---
## Architecture & Tech Stack

### Backend Architecture
- **Core:** Java 21 / Spring Boot 3.x
- **Security:** Spring Security & JWT (JSON Web Tokens)
- **Database & Persistence:** PostgreSQL / Spring Data JPA (Hibernate)
- **Build Tool:** Maven

### Frontend Client
- React
- Vite
- CSS (Vanilla)

---

## Core System Design

### Database Layout
The underlying transactional model relies on a tightly constrained relational schema optimising indexing and data isolation for user records. View the full architectural design, entity relationships, and database constraints in the [design documentation](https://github.com/Z33xD/finance-tracker/blob/main/project/DESIGN.md).

### Protected API Architecture

All core REST resources expect a valid JWT passed via the Authorisation: Bearer header, filtering resources inherently by the active token context:

| **Endpoint**    | **HTTP Method**           | **Scope**                                                  |
| --------------- | ------------------------- | ---------------------------------------------------------- |
| /auth/register  | POST                      | Public: Registers new user + triggers verification email   |
| /auth/login     | POST                      | Public: Authenticates identity and issues JWT access token |
| /accounts       | GET / POST / PUT / DELETE | Secured: User-scoped financial assets                      |
| /transactions   | GET / POST / DELETE       | Secured: Financial ledger management                       |
| /categories     | GET / POST                | Secured: Custom categorisations                            |
| /budgets        | GET / POST / PUT          | Secured: Spending caps and allocations                     |
| /import-batches | GET / POST                | Secured: Multi-line CSV parsing and logs                   |

### Batch CSV Ingestion
`POST /api/import-batches/upload` ingests transactions from a CSV payload (`multipart/form-data` with a target `account_id`). The pipeline is staged, not one-shot:
1. **Load & stage** — the file is parsed with `commons-csv` into an `import_batches` row (`status = pending`) that records the ingest job.
2. **Row-wise ETL** — each record is parsed (`category_id`, `amount`, `transaction_date`, `description`, `transaction_type`), validated, and de-duplicated against existing rows by `(date, amount, description, category_id, type)` before it is written to `transactions`.
3. **Ledger effect** — accepted rows are committed through the same transactional balance path as manual entries, so account balances stay consistent.
4. **Job metrics** — the batch is finalised with `total_records` / `successful_records` / `failed_records` and a terminal `processed` / `failed` status, giving per-file audit and error-count telemetry via `GET /api/import-batches`.

---

## Configuration & Environment Variables

To run this application locally, you must provide the environment configurations for database access, the mail server (for sign-up verification), and the exchange rate client.
```
JWT_SECRET_KEY=your_jwt_secret_key
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password  
EXCHANGERATE_API_KEY=your_exchangerate_api_key
SUPPORT_EMAIL=support@email.com
APP_PASSWORD=apppassword
```

---

## Getting Started

### 1. Clone the Project
```
git clone https://github.com/Z33xD/finance-tracker.git
cd finance-tracker
```

### 2. Run the Backend Infrastructure
Ensure your PostgreSQL instance is running and your environment variables are initialised.
```
mvn spring-boot:run
```
The API engine, defaults to: http://localhost:8080

### 3. Run the Frontend Client
```
cd frontend
npm install
npm run dev
```
The frontend, by default, runs on: http://localhost:5173/

---

## Keep-Alive — Render & Supabase Free Tier

This project is deployed on Render (free tier) with Supabase Postgres (free tier). Both tiers pause on inactivity, so keep-alive probes are required.

### Endpoints

| Endpoint | Purpose | Auth | Implementation |
|----------|---------|------|----------------|
| `GET /health` | Lightweight liveness probe — returns `{"status":"ok"}` with **no DB call**. Also available as `GET /actuator/health` via Spring Boot Actuator. | `permitAll` | `health.HealthController` |
| `GET /health/db` | DB keep-alive — runs `SELECT 1` via `JdbcTemplate` and returns `{"status":"db_ok"}` (or `503` with `{"status":"db_error"}` on failure). | `permitAll` | `health.HealthController` → `health.DatabaseHealthService` |

Both paths are explicitly whitelisted in `config.SecurityConfiguration` and skipped in `config.JwtAuthenticationFilter#shouldNotFilter`.

An internal backup is also scheduled in `config.SchedulingConfiguration`:

```java
@Scheduled(fixedRate = 259200000) // every 3 days
public void pingDatabasePeriodically() { databaseHealthService.pingDatabase(); }
```

> **Note:** The `@Scheduled` ping only fires while the JVM is actually running. Render free tier **suspends the whole process** on spin-down, so this scheduler cannot wake a sleeping instance on its own — it is purely a backup while the app is already being kept awake externally.

### External pinger setup (required outside this codebase)

You must configure an **external** scheduled pinger. Two independent schedules are recommended:

**a) Render — prevent spin-down (every 10–14 minutes)**

Render free web services sleep after **~15 minutes** of no inbound traffic (cold start on next request). Configure one of these to hit `GET /health` (or `/actuator/health`) every **10–14 minutes** — staying safely under the 15-minute idle threshold:

- [cron-job.org](https://cron-job.org) — create a job with URL `https://<your-app>.onrender.com/health`, interval 10–14 min, GET.
- [UptimeRobot](https://uptimerobot.com) — monitor type HTTP(s), interval 10 min (or 5 min on paid plan), keyword `ok`.
- **GitHub Actions workflow** (cron trigger) — add `.github/workflows/keep-alive.yml`:

```yaml
name: keep-alive
on:
  schedule:
    - cron: '*/12 * * * *'  # every 12 minutes
  workflow_dispatch:
jobs:
  ping:
    runs-on: ubuntu-latest
    steps:
      - run: curl -fsS https://<your-app>.onrender.com/health
```

Use the lightweight `/health` (not `/health/db`) for this frequent ping to avoid unnecessary DB load.

**b) Supabase — prevent database pausing (every 3–4 days)**

Supabase free projects pause after **~7 days** of database inactivity (no queries). Configure a second, much less frequent job to hit `GET /health/db` every **3–4 days**:

- Same providers as above — e.g. a second cron-job.org job at `https://<your-app>.onrender.com/health/db` on a 3-day schedule, or a GitHub Actions workflow with `cron: '0 9 */3 * *'` (every 3 days at 09:00 UTC).

The internal `@Scheduled(fixedRate = 259200000)` (3-day) DB ping in `SchedulingConfiguration` reuses `DatabaseHealthService.pingDatabase()` as a best-effort backup, but should not be relied on alone for the reason noted above.

---
