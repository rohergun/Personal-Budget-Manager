# Architecture

This document covers how BudgetManager is structured internally: the project layout, the responsibilities of each layer, the REST API surface, and the reasoning behind the architectural choices that shape the codebase.


For reasoning specific to data modeling and schema tradeoffs (entity relationships, cascade behavior, constraints), see [DESIGN DECISIONS](DECISIONS.md).

## Table of Contents

- [Project Structure](#project-structure)
- [Layers](#layers)
- [UML Diagram](#uml-diagram)
- [REST API Reference](#rest-api-reference)
- [Architecture Decisions](#architecture-decisions)


## Project Structure

The codebase follows a **package-by-feature** layout rather than package-by-layer — each business domain owns its entity, repository, service, controller, DTOs, and mapper in one place, instead of scattering related code across global `controllers/`, `services/`, `repositories/` packages.

```
src/main/java/io/github/rohergun/budgetmanager/
├── auth/                  # Registration and login
│   └── dto/
├── security/              # JWT filter, JwtService, CustomUserDetails(Service), SecurityConfig
├── user/                  # AppUser entity, profile management
│   └── dto/
├── category/               # User-owned expense/income categories
│   └── dto/
├── budget/                 # Monthly limits per category
│   └── dto/
├── transaction/             # Income/expense entries
│   └── dto/
├── financialgoal/           # Savings goals and contributions
│   └── dto/
├── summary/                 # Cross-domain reporting (monthly summaries)
│   └── dto/
├── exception/               # DomainException, DomainErrorMessage, GlobalExceptionHandler
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

## UML Diagram

_UML class diagram to be added here._

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