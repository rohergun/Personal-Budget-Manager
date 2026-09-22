# 8. Split configuration into dev/prod Spring profiles

## Status
Accepted

## Context
`application.yaml` held every setting like datasource, JWT secret, logging, actuator in one file, with `${VAR:fallback}` defaults on secrets so the app would start locally with zero setup. 

The same fallbacks applied when running on Render: if `DATABASE_URL` or `JWT_SECRET` were ever missing or misspelled in the platform's environment, the app would silently start with the local dev Postgres URL or the checked-in default JWT secret instead of failing. 

There was also no distinction between localdev conveniences and what should be exposed once the app is live on Render.

## Decision
Split configuration into a profile-agnostic base and two profiles, selected by `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}` in `application.yaml` (so local runs default to `dev`, and Render is configured with `SPRING_PROFILES_ACTIVE=prod`):

- `application.yaml` settings identical across environments: app name, cache type, JPA `open-in-view`/`ddl-auto`, Flyway baseline config, springdoc paths, actuator endpoint exposure, `info`.
- `application-dev.yaml` datasource, JWT secret/expiration all keep `${VAR:fallback}` defaults matching `compose.yaml`, so `./mvnw spring-boot:run` works with no environment setup. Also turns on `show-sql`, Hibernate statistics, and full actuator health details.
- `application-prod.yaml` the same keys with **no fallback**: `${DATABASE_URL}`, `${DATABASE_USERNAME}`, `${DATABASE_PASSWORD}`, `${JWT_SECRET}`. If Render's environment is missing any of these, the app fails to start instead of running with a wrong or insecure default. SQL logging and Hibernate statistics are off.
- Added the `me.paulschwarz:spring-dotenv` dependency so an optional local `.env` file (see `.env.example`, `.env` itself gitignored) is loaded into the environment automatically. 

**Health endpoint**: `/actuator/health` stays `permitAll` in `SecurityConfig` in both profiles (Render's own health checks hit it without credentials). What changes per-profile is `show-details`/`show-components`: `always` in dev, `when-authorized` in prod.

## Other Considerations

**Adding a role-based restriction (`management.endpoint.health.roles`) on top of `when-authorized`**
- Would let only admin users see health details instead of any authenticated user.
- Rejected for now the app has no role/authority system. 

**Requiring authentication on `/actuator/health` itself instead of gating details**
- Simpler security model.
- Rejected, Render's platform health check calls this endpoint without credentials. 

## Consequences

- Local development is unaffected no `.env` file is required, defaults match the existing `compose.yaml` Postgres container.
- Render's environment **must** define `SPRING_PROFILES_ACTIVE=prod`, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET`.
- Full health details needs valid JWT so `/actuator/health` on the live deployment now returns only `{"status":"UP"}` to anonymous requests. 
