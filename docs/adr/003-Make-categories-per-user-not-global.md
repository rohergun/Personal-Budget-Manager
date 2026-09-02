# 3. Make categories per-user, not global

## Status
Accepted

## Context

At the start of the project Category class designed as global as some kinda list that each user have it by default
like (Food, Entertainment, Rent, Investment, ..)

This was simple but having a user controlled categories would make the logic more flexible.

## Decision

Adding a user_id foreign key to Category, and implement unique constraints on user_id and category_name
so user can't duplicate their own category but can implement as their needs.

## Consequences

- CRUD management to Category by User.
- Every query for category has to check for ownership. 
