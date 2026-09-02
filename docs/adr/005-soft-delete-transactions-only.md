# 5. Soft delete transactions only, hard delete everything else

## Status
Accepted


## Context
Deleting transactions has more drastic effect on user's finance then budget or categories.

## Decision
Add a nullable `deletedAt` timestamp to `Transaction` only. Deleting a transaction sets this field instead of removing the row; every
transaction query filters on `deletedAt IS NULL`. `Category`, `Budget`, and `FinancialGoal` keep hard deletes.

## Other Considerations
**Soft delete everywhere**
- Same recovery benefit for categories, budgets, and goals.
- Rejected — those entities have unique constraints (name, category)
  that get more complex with soft-deleted rows, for less benefit than
  transactions get.

**Hard delete everywhere**
- Simpler, no query filtering needed.
- Rejected for transactions — it's the entity users are most likely
  to delete by accident.

## Consequences
- An accidentally deleted transaction can be recovered later, since the row still exists.
- Every transaction query — lookups, lists, the monthly summary aggregation has to include the `deletedAt IS NULL` filter.
Missing it in even one place would let "deleted" transactions silently reappear, so every query was updated deliberately rather than relying on a default.

