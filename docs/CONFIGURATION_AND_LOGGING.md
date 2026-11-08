# Externalized Configuration, Logging and Exception Handling

## Externalized configuration

The deployable JAR is separated from environment-specific configuration.

- `local`: H2 in-memory; `create-drop` for local convenience.
- `dev`, `uat`, `prod`: MySQL; `ddl-auto=validate`.
- External overrides live at `./config/application-{profile}.yml`.
- Credentials come from environment variables or a secrets manager, never source control.
- The same built JAR can be promoted through environments.

Build:

```bash
mvn clean package
```

Run locally:

```bash
java -jar target/course-registration-service-1.0.0.jar
```

Run DEV:

```bash
export DB_URL='jdbc:mysql://localhost:5432/course_waitlist'
export DB_USERNAME='course_waitlist_app'
export DB_PASSWORD='change-me'
java -jar target/course-registration-service-1.0.0.jar \
  --spring.profiles.active=dev \
  --spring.config.additional-location=optional:file:./config/
```

For UAT/PROD inject the same variables from the deployment secret manager.
Use Flyway or Liquibase migrations as a controlled release step before deploying to
shared environments; `ddl-auto=validate` checks mappings but does not evolve schemas.

## Logging

Logback writes to console and rolling files. `LOG_DIR` defaults to `./logs`;
`LOG_LEVEL` defaults to `INFO`. Files rotate daily and at 20 MB, retain 14 days,
and cap archives at 1 GB.

`X-Correlation-ID` is accepted only in a safe restricted format; otherwise a UUID is
generated. The ID is returned in the response header and included in logs via MDC.
Business rejections are logged at WARN; unexpected errors include server-side stack traces.

Never log passwords, tokens, full request bodies, or unnecessary learner personal data.
In production, forward logs to a centralized logging platform.

## Exception handling

`ApiExceptionHandler` maps failures to stable API responses:

| Failure | HTTP | Code |
|---|---:|---|
| Business rule conflict | 409 | Domain-specific |
| Missing resource | 404 | Domain-specific |
| Bean validation | 400 | `VALIDATION_FAILED` |
| Malformed JSON | 400 | `MALFORMED_REQUEST` |
| Invalid parameter | 400 | `INVALID_REQUEST` |
| Unsupported HTTP method | 405 | `METHOD_NOT_ALLOWED` |
| Database constraint conflict | 409 | `DATA_CONFLICT` |
| Unexpected exception | 500 | `INTERNAL_ERROR` |

Internal exception details are not exposed to clients. The response includes a correlation ID
for support and log investigation.

## Operational endpoints

Actuator exposes health, info and metrics. Secure these endpoints with network controls and
authentication in deployed environments.
