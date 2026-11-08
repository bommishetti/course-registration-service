# MySQL setup for DEV / UAT / PROD profiles

Local development continues to use the in-memory H2 database. The shared `dev`, `uat`,
and `prod` Spring profiles use MySQL.

## 1. Create a database and application user

Run these statements as a MySQL administrator, adapting names and passwords to your environment:

```sql
CREATE DATABASE course_waitlist
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'course_waitlist_app'@'%' IDENTIFIED BY 'replace-with-a-secret';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON course_waitlist.* TO 'course_waitlist_app'@'%';

FLUSH PRIVILEGES;
```

For production, use a restricted host instead of `%`, grant only the privileges required by
the deployment/migration process, and keep migration privileges separate from runtime privileges.

## 2. Configure environment variables

```bash
export DB_URL='jdbc:mysql://localhost:3306/course_waitlist?useSSL=true&serverTimezone=UTC'
export DB_USERNAME='course_waitlist_app'
export DB_PASSWORD='replace-with-a-secret'
```

Use TLS settings appropriate to your MySQL server and certificate configuration. Do not disable
TLS verification in production.

## 3. Run the application

```bash
mvn clean package
java -jar target/course-registration-service-1.0.0.jar \
  --spring.profiles.active=dev \
  --spring.config.additional-location=optional:file:./config/
```

For UAT and PROD, change the active profile and inject environment-specific credentials securely.

## 4. Schema management

The MySQL profiles use `ddl-auto=validate`. This checks that entity mappings match the existing
schema; it does not create or migrate tables. Add Flyway or Liquibase migrations as a controlled
deployment step before using the shared environments.

## 5. Compatibility notes

- H2 remains the local default and is not a substitute for MySQL integration testing.
- Test database-specific indexes, locking behavior, isolation levels, timestamp handling and
  constraint behavior against the same MySQL major version used in UAT/production.
- The current code uses JPA and portable repository operations, but H2 passing tests do not
  prove MySQL behavior is identical.
