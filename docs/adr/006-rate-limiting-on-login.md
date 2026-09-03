# 7. Rate limit login attempts with an in-memory token bucket

## Status
Accepted

## Context
`auth/login` endpoint had no protection for the repeated attempts.

## Decision
Add rate limiting on login endpoint using Bucket4j's token bucket algorithm with current client IP as a key.

If requests gets over the set limit client get a `429 Too Many Requests` response.


## Other Considerations

**Sliding window log**
- Rejected as unnecessary it costs more memory per client for precision this project doesn't need
- Token bucket's approximation is accurate enough for slowing down brute-force login attempts.

**Rate limiting at an API gateway with a shared store**
- Not applicable, there's no gateway or multiple instances here to coordinate between
- If app scale in that way, moving to rate limiting with api gateway is possible.


## Consequences

- Buckets live in the apps own memory no seperate service is required or connect to like redis.
- Current rate-limiting implementation protecting against single IP, not for someone has access to more IPs. 
- If the app need to ran as multiple instances, this would need to move to a shared storage like Redis.