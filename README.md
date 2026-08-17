[![Backend CI](https://github.com/rohergun/Personal-Budget-Manager/actions/workflows/build.yaml/badge.svg)](https://github.com/rohergun/Personal-Budget-Manager/actions/workflows/build.yaml)

# Personal BudgetManager 
A personal finance management REST API built for students or professionals to track expenses, manage budgets, and work toward financial goals.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Documentation](#documentation)
- [Testing](#testing)
- [Roadmap](#roadmap)


## Introduction

BudgetManger is a backend service for personal finance app.
It handles user authentication, expense and income tracking, category-based budgeting, and financial goal management, with monthly spending summaries tying transactions and budgets together.

### Tech Stack & Tools

- Java 21
- Spring Boot
- Spring Security (JWT authentication)
- Spring Data JPA / Hibernate
- Spring Cache (Caffeine)
- PostgreSQL
- H2 Database (in-memory, for tests)
- Docker & Docker Compose
- Maven
- Jakarta Validation
- Lombok
- MapStruct (entity ↔ DTO mapping)
- JJWT (JWT generation/validation)
- JUnit 5 & Mockito (unit and controller tests)
- springdoc-openapi (Swagger UI)
- Javascript, Bootstrap & HTML 


## Features

- **User authentication** — registration and login secured with JWT (stateless, no server-side session)
- **User profile management** — view/update profile, change password, delete account
- **Categories** — user-owned, uniquely named expense/income categories
- **Budgets** — one monthly limit per category, per user
- **Transactions** — income and expense entries tied to a category, with date validation (no future-dated entries)
- **Financial goals** — savings targets with progress tracking via a dedicated contribution endpoint
- **Monthly summaries** — aggregated income, expenses, and per-category spending vs. budget for a given month
- **Consistent error handling** — a global exception handler returning structured, predictable error responses
- **Ownership-scoped access** — every resource lookup is scoped to the authenticated user; no user can read or modify another user's data
- **Static web dashboard** — login/register/dashboard UI showing the API's data live, without needing Swagger to see it in action
- **Response caching** — Caching on the monthly summary endpoint, with automatic invalidation whenever a transaction or budget changes
## Getting Started

### Prerequisites

- Java 21
- Maven (or the included `./mvnw` wrapper)
- Docker and Docker Compose (for PostgreSQL and pgAdmin)
### Installation

1. **Clone the repository**
```bash
   git clone https://github.com/rohergun/BudgetManager.git
   cd BudgetManager
```

2. **Start the database**
```bash
   docker compose up -d
```
This starts PostgreSQL and pgAdmin as defined in `docker-compose.yaml`.

3. **Configure environment values**

Copy the example configuration and fill in your own values (JWT secret, DB credentials if changed from defaults).

4. **Run the application**
```bash
   ./mvnw spring-boot:run
```

5. **Optional Try Dashboard**

A minimal login/register/dashboard UI is served at:
```
   http://localhost:8080/login.html
```
Register an account, log in, and the dashboard will show your data pulled live from the API.

6. **Explore the API**
   Swagger UI is available at:
```
   http://localhost:8080/swagger-ui/index.html
```

7. **Run the tests**
```bash
   ./mvnw clean test
```
Tests run against an in-memory H2 database, so Docker is not required to run the test suite.

## Documentation

This README covers what the project does and how to run it. Deeper technical documentation lives in `docs/`:

- **[Architecture](docs/ARCHITECTURE.md)** — project structure, layers, class diagram and architecture decisions section. 

- **[Design Decisions](docs/DECISIONS.md)** — project system design, key design decisions, restapi design decisions

## Testing

- **Unit tests** (JUnit 5 + Mockito) cover every service's business logic: ownership checks, uniqueness rules, and domain exception paths.
- **Controller tests** (`@WebMvcTest` + MockMvc) cover request validation, status codes, and correct delegation to the service layer, with Spring Security's test support used to simulate an authenticated principal.
- **Context load test** (`@SpringBootTest`) runs against an in-memory H2 database (profile `test`), so the full test suite runs without requiring Docker/PostgreSQL.

## Roadmap

- [x] Read-through caching on low-write, high-read endpoints 
- [ ] More summary options for users and Goal contribution
- [ ] CSV imports
- [ ] Export report generation
- [ ] Recurring Transfers (using @Scheduled, auto creating Transactions)
- [ ] Rate limiting on auth endpoints 
- [ ] Deployment
