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

## Active Development

- [ ] Finalising token authentication filters and routing controller debugging.
- [ ] Enhancing the user interface.
- [ ] Implementing automated recurring transaction schedulers.
- [ ] Enhancing the data visualisation layer for predictive budgeting analytics.
- [ ] Dockerising the application

---
