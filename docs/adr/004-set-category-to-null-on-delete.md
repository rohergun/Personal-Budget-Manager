# 5. Set category to null on deletion, instead of cascade or restrict

## Status
Accepted

## Context
Budget and Transaction classes have a reference to Category via a foreign key.

When a user deletes a category that's still in use, the database has to do something with those now-orphaned references.

## Decision
Use `ON DELETE SET NULL`.

Deleting a category sets `category_id` to `null` on any budget or transaction that referenced it, rather than
cascade deleting rows or restricting the deletion.

## Other Considerations
**Cascade delete**
Deleting a category would silently delete every budget and transaction that referenced it.

**Rejected**, not loosing transactions for user more important than organizing spending's.

## Consequences
- Deleting a category is immediate and never destroys transaction history.
- Affected budgets and transactions become "uncategorized", summary service handles so uncategorized entities are not ignored.
- No cleanup step is forced on the user before deleting a category they no longer use.