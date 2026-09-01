# 1. Use SQL Over NoSQL database

## Status
Accepted

## Context
BudgetManager has a clear defined relation between entities, like user has transactions, budgets, financial goals,
transactions has some category where category has some user defined budget.

For example : User sets a 500$ budget for Food & Entertainment category, each transaction carries some kinda 
category like this. 

## Decision
Used relational database PostgreSQL.

## Outcomes

- Foreign keys and constraints enforce relationships and rules, like one budget per category.
- Making summary is simpler due to table joins between entities and data is already structured for it.

## Trade-Offs

- If new field needs to be defined table structure must be altered, this is acceptable tradeoff because entities well-defined upfront.  
