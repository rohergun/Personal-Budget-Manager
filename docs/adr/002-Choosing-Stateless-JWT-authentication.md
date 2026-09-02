# Choosing Stateless JWT Authentication

## Status
Accepted

## Context
Use JWT instead of server-side sessions, since this is a straightforward
REST API and simplicity.

## Other Considerations
**OAuth2** 
- Project structured as backend system communicating with REST API and no need for introducing third party logins.
- If separate frontend built later OAuth2 would be worth adding.

**Session-based authentication** 
- Requires Server side session like some in-memory implementation or with Redis. Compare to JWT there is an infrastructure cost.
- Main advantage would be session allows the admin to cut off user instantly, but project designed is more personal spending-tracker rather then platform with many users.


## Consequences
- No server-side login tracking needed — the app stays simple to run,
with nothing extra to set up just to remember who's logged in.
- Token itself carries the user credentials so no need to store in database.

## Trade-Offs
- There is no instantly way log user out or cut their access, but for project scope and design this is an acceptable tradeoff.