# Architecture

This document covers how BudgetManager is structured internally: the project layout, the responsibilities of each layer, the REST API surface, and the reasoning behind the architectural choices that shape the codebase.


For reasoning specific to data modeling and schema tradeoffs (entity relationships, cascade behavior, constraints), see [DESIGN DECISIONS](DECISIONS.md).

## Table of Contents

- [Project Structure](#project-structure)
- [Frontend Dashboard](#frontend-dashboard)
- [Architecture Decisions](#architecture-decisions)


## Project Structure

### Class Diagram

See the UML diagram: [Class Diagram](class-diagram.mmd)

### Project Layers

The codebase follows a **package-by-feature** layout rather than package-by-layer — each business domain owns its entity, repository, service, controller, DTOs, and mapper in one place, instead of scattering related code across global `controllers/`, `services/`, `repositories/` packages.

```
src/main/java/io/github/rohergun/budgetmanager/
├── auth/                  # Registration and login, authentication(Service, Controller)
│   └── dto/
├── security/              # JWT filter, JwtService, CustomUserDetails(Service), SecurityConfig
│
│── model/                 # BaseEntity(Parent class for other Entities, supplies id and timestamp for objects)
│ 
├── user/                  # AppUser entity, profile management
│   └── dto/
├── category/              # User-owned expense/income categories
│   └── dto/
├── budget/                # Monthly budgets/limits per category
│   └── dto/
├── transaction/           # Income/expense entries
│   └── dto/
├── financialgoal/         # Savings goals and contributions
│   └── dto/
├── summary/               # Cross-domain reporting (monthly summaries, read only)
│   └── dto/
├── exception/             # DomainException, DomainErrorMessage, GlobalExceptionHandler
└── BudgetManagerApplication.java
```


Each feature package generally contains:
- `<Entity>.java` — the JPA entity
- `<Entity>Repository.java` — Spring Data JPA repository
- `<Entity>Service.java` / `<Entity>ServiceImpl.java` — business logic, ownership checks, domain rules
- `<Entity>Controller.java` — REST endpoints
- `<Entity>Mapper.java` — MapStruct entity-to-DTO mapping
- `dto/` — request/response records
- `summary/` is the one package that doesn't follow this shape exactly — it has no entity or repository of its own, since it's a read-only aggregation layer over `transaction` and `budget` data. See [Architecture Decisions](#architecture-decisions) below.

## Frontend Dashboard

A minimal static frontend lives alongside the backend and is served directly by Spring Boot — no separate app, no build step, no framework.

```
src/main/resources/static/
├── login.html
├── register.html
├── index.html      # redirects to login.html page
├── dashboard.html 
├── css/
│   └── style.css
└── js/
├── api.js          # shared fetch wrapper, attaches the JWT to every request
├── auth.js         # login, register, logout, token storage, auth guard
├── login.js
├── register.js
└── dashboard.js
```

Plain JavaScript (ES modules), Bootstrap via CDN. 

The pages call the same REST API described below — nothing here is a special "frontend endpoint," it's the same `/api/v1/...` surface Swagger uses. 

The JWT is stored in the browser's `localStorage` after login/register and attached as `Authorization: Bearer <token>` on every subsequent request.

The dashboard exists to make the API's behavior visible, not to be a full application — see [Design Decisions](DECISIONS.md) for why it's scoped this way.


## Architecture Decisions

* **Package-by-feature, not package-by-layer**

Everything about one domain — `Budget`'s entity, repository, service, controller, 
DTOs — lives in one `budget/` package. Adding a feature to `Budget` never means touching four unrelated top-level folders

* **Entities never leave the service layer.**

Controllers only ever return DTOs. This keeps password hashes and other internal fields out of responses,
and avoids crashes from serializing lazy-loaded fields outside a database session.

* **One exception type for all domain errors.**

Instead of a separate exception class per error case, `DomainException` just wraps an enum value (`DomainErrorMessage`) that carries the message and the HTTP status together.

* **The logged-in user's ID always comes from the JWT, never from the request.**

Every service method takes the user ID as a parameter, but that ID is pulled from `@AuthenticationPrincipal` in the controller — never trusted 
from a path variable or request body. This makes it structurally impossible to act on someone else's data by simply passing a different ID.

* **Services don't know about Spring Security.**

They take a plain `UUID`, not an `Authentication` object. The security context is resolved once, in the controller, and handed down. 
This keeps services easy to unit test and keeps them usable outside a web request if that's ever needed.

* **`summary` reads other domains' repositories directly, instead of going through their services.** 

This is the one deliberate exception to normal layering — `SummaryService` exists purely to aggregate data across `Transaction` and `Budget` for reporting.

Which is a different job from owning either domain. Keeping it separate means `TransactionService`/`BudgetService`
stay focused on their own CRUD, and future reports (yearly summaries, spending trends) have one natural place to live.

* **Ownership failures return 404, not 403.** 

If a resource exists but belongs to someone else, the API responds as if it doesn't exist at all, instead of confirming its existence with a 403. 

Not leaking "this ID belongs to someone" is worth more than the marginal clarity a 403 would add.