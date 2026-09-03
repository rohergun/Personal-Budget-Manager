[![Backend CI](https://github.com/rohergun/Personal-Budget-Manager/actions/workflows/build.yaml/badge.svg)](https://github.com/rohergun/Personal-Budget-Manager/actions/workflows/build.yaml)
[![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)](https://personal-budget-manager-vwce.onrender.com/)

# Personal BudgetManager 
A personal finance management REST API built for students or professionals to track expenses, manage budgets, and work toward financial goals.

*Note: hosted on a free tier — the first request after a period of inactivity may take up to a minute to respond while the service wakes up.*

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Documentation](#documentation)
- [Roadmap](#roadmap)


## Introduction

BudgetManager is a **Backend System** and provides **REST APIs** for personal finance app.

It handles user authentication, expense and income tracking, category-based budgeting, and financial goal management,
with monthly spending summaries tying transactions and budgets together.

## Features

- User authentication (Stateless authentication using JWT).
- Rate limiting on login attempts.
- Full CRUD management of  Transactions, Categories, Budgets, Financial Goals.
- Users can read monthly summary of net transactions (incomes, expenses) against user set budgets.
- Transactions can be categorized by users.
- Dashboard summarizing account activity. 
- Faster response time for frequently accessed endpoints via caching.
- Testing suits for service and controller layers, automated with CI/CD.
- Containerized with Docker and deployed live with interactive API documentation.
- Health check endpoints for monitoring application status.

### Tech Stack

- Java 21
- Spring Boot
- Spring Security 
- Spring Data JPA / Hibernate
- Spring Cache
- PostgreSQL
- H2 Database (in-memory, for tests)
- Docker
- JUnit 5 & Mockito (unit and controller tests)
- springdoc-openapi (Swagger UI)
- Spring Boot Actuator
- Bucket4J
- Render


## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker Compose for local development
### Installation

1. **Clone the repository**
```bash
   git clone https://github.com/rohergun/Personal-Budget-Manager
   cd BudgetManager
```

2. **Start the database**

Make sure docker is running.

```bash
   docker compose up -d
```

3. **Run the application**
```bash
   ./mvnw clean spring-boot:run
```

4. **Testing**

For testing, docker is not required all testing done with in-memory H2 Database. 

```bash
  ./mvnw clean test
```

5. **Explore API locally**

```
   http://localhost:8080/swagger-ui/index.html
```


## Documentation

- [Class Diagram](docs/class-diagram.mmd) - class diagram with entity relationships

- [API Docs](https://personal-budget-manager-vwce.onrender.com/swagger-ui/index.html) - live, interactive Swagger UI

- [Architecture Documentation](docs/) - records of architecture decisions

## Roadmap

- [x] *Read-through caching on low-write, high-read endpoints*
- [x] *Deployment*
- [x] *Rate limiting on auth endpoints*
- [ ] Recurring Transfers (using @Scheduled, auto creating Transactions)
