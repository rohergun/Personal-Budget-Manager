# Design Decisions

## Table of Contents

- [System Design](#system-design)
- [Key Design Decisions](#key-design-decisions)
- [REST API Design](#rest-api-design)


## System Design

- **Relational Database over NoSQL database**

In BudgetManager data is naturally relational a user has many budgets, transactions, and goals, and each of those references a category. 

Foreign keys and constraints handle that shape well, and rules like "one budget per category per user" are enforced reliably at the database level rather than only in application code.

- **Stateless JWT authentication, no server-side sessions**

The server doesn't store any login state. Each request carries a signed token proving who the user is.

<br>

## Key Design Decisions

- **Categories belong to a single user, not shared globally.**

First idea was shared universal category list like Foods, Rent, Transport that will used by all the users in the app.

That was dropped to give more flexibility to each user's personal needs. 

Each category has an owner, and a user can't have two categories with the same name.

```java
// Category.java
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_name"}))
```

- **A user can only have one budget per category.**

Two separate "Food" budgets would be ambiguous — which one applies? 
This is enforced with a database constraint, not just application logic, so it can't be bypassed.

To change a limit, you update the existing budget instead of creating a new one.

```java
// Budget.java
@Table(name = "budgets", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id"}))
```

- **Deleting a category doesn't delete your spending history.**

If a user deletes a category, any budgets or transactions that used it just become "uncategorized" instead of being deleted or blocking the deletion.

Losing a user's transaction history is worse than showing an uncategorized entry, so this was chosen over forcing the user to reassign everything first, or silently deleting their records.

```java
// Budget.java / Transaction.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
@OnDelete(action = OnDeleteAction.SET_NULL)
private Category category;
```

- **Contributing to a goal adds to its progress, it doesn't replace it.**

Instead of asking the client to calculate and send a new total, the "add a contribution" endpoint takes an amount and adds it to what's already saved. 

This avoids bugs from two contributions happening close together, and matches how a user actually thinks about it

- **Shared fields (`id`, `createdAt`, `updatedAt`) live in one base class**

Inherited by every entity, instead of being repeated five times. 

Timestamps are set automatically, not manually, so they can't be forgotten or set inconsistently.

```java
// BaseEntity.java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}
```

<br>

## Rest API Design

<br>

- **Every endpoint that needs a user is scoped to the logged-in user.** 

The user's ID always comes from their token, never from something the client sends. 

A user can never read or change another user's data by guessing an ID.

```java
// CategoryController.java
public ResponseEntity<CategoryResponse> getCategory(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable UUID id) {
    return ResponseEntity.ok(categoryService.getCategoryById(principal.getId(), id));
}
```
<br>

- **Ownership failures return 404, not 403.**

If you ask for a resource that exists but isn't yours, the API says "not found," not "forbidden."

A 403 would confirm the resource exists somewhere — 404 doesn't leak that information.

<br>

- **`/me` is only used for a resource a user has exactly one of.** 

The user's own profile is `/users/me`, since there's only ever one. 

Everything a user can have many of — categories, budgets, transactions, goals — uses `/{id}` instead,
with ownership checked on the server rather than baked into the URL.

<br>

- **The monthly summary defaults to the current month if none is given.**

`GET /summaries/monthly` with no parameters answers the most common question 
("how am I doing this month?") without the client needing to know today's date.

An explicit `?month=2026-08` overrides it.

