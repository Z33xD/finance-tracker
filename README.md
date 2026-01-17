# Finance Tracker Backend

Spring Boot REST API for managing financial data

---

## Overview 

This is a personal project made by [me](https://github.com/Z33xD/). It is a Spring Boot based backend application, that provides RESTFul APIs for managing financial data, including `users`, `accounts`, `transactions`, `categories`, `budgets` and `exchange_rates`.  This application is built on top of a pre-designed relational database made with PostgreSQL.

This project was built mainly to get hands-on experience with a database management system like Postgres and  Spring Boot to build production-style backend APIs.

---

## Scope

This application can:
* Providing CRUD functionalities over the finance-tracker database.
* Validating and persisting financial data
* Handling CSV-based bulk transactional imports
* Performing currency-conversion using an external exchange-rate data

What's out of scope for this project (for now) is:
* User-facing front-end
* Authentication beyond basic credential handling
* Financial analytics, forecasting or analytics

---

## Features

* CRUD operations for `users`, `accounts`, `categories`, `transactions`, and `budgets`
* CSV upload endpoints for bulk `transaction` imports, with duplicate detection and `import_batch` tracking
* Currency conversion using [ExchangeRate-API](https://www.exchangerate-api.com/)
* RESTFul API design with clear resource boundaries
* Validation and basic error-handling for incoming requests

---

## Tech Stack
* Java 21
* Spring Boot
* PostgreSQL
* ExchangeRate-API
* Maven

---

## Database Design

The full database design, including:
* Entity definitions
* Relationships
* Constraints and indexes
* Design decisions and limitations

can be found in [this document](./project/DESIGN.md)

---

## API Overview

The application provides RESTFul endpoints for the following resources:
* `/users`
* `/accounts`
* `/categories`
* `/transactions`
* `/budgets`
* `/import-batches`
* `/exchange-rates`

All endpoints accept and return JSON.

---

## CSV Import
This application has an endpoint to upload CSV files containing transactions-related data. Its key behaviours are:
* Transactions are processed in an import batch
* A record of total, successful and failed imports are kept per batch
* Duplicate transactions are skipped and counted as a failed transaction
* Errors during processing are captured per batch

---

## Configuration and Setup

The application requires the following environment variables
- Database connection credentials (`SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_USERNAME`)
- ExchangeRate API Key (`EXCHANGERATE_API_KEY`)

---

## Running the Application

1. Clone the repository
2. Configure the database and the above mentioned API Keys
3. Run the application with
```bash
  mvn spring-boot:run
```

The API will be available at http://localhost:8080

---

## Limitations and Future Work

- No support for recurring transactions
- No real-time exchange rate updates
- Limited authentication and authorisation
- No audit logging for data changes
