# Course Registration & FIFO Waitlist

A bounded Java 17 / Spring Boot implementation of the supplied AI-Assisted Engineering Assessment. It implements one course offering's enrollment, drop, prerequisite validation, and FIFO waitlist promotion. It intentionally does not implement a frontend, course catalog, payments, identity integration, multi-term registration, or deployment.

## Technology

- Java 17
- Spring Boot 3.4.5, Spring Web, Spring Data JPA, Bean Validation
- H2 in-memory database (easy local demonstration)
- JUnit 5, AssertJ, Mockito, Spring Boot Test
- Maven

## Business assumptions and policies

1. A learner must have completed the course prerequisite before enrolling or joining the waitlist.
2. A learner can have only one registration record per course offering. A dropped or skipped learner cannot submit a second registration for the same offering; this makes duplicate requests deterministic and preserves the assessment's “may enroll only once” rule.
3. When full, an eligible learner is added to the waitlist exactly once.
4. Waitlist order is FIFO by `createdAt`, with database-generated registration ID as a deterministic tie-breaker.
5. Eligibility is checked again at promotion time. If a waitlisted learner has become ineligible, that entry is marked `SKIPPED` and the next eligible learner is considered. The skipped record is retained for auditability.
6. One seat is promoted per successful drop. The course row is pessimistically locked for all enrollment/drop operations, serializing changes for the same offering. Different offerings can proceed independently.
7. Capacity is defined per course offering and must be at least one.

## Run locally

Prerequisites: JDK 17+ and Maven 3.9+.

```bash
mvn clean test
mvn spring-boot:run
```

The service starts at `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:coursewaitlist`, username `sa`, blank password).

## Quick API walkthrough

Create one course with capacity 2:

```bash
curl -X POST http://localhost:8080/api/courses -H 'Content-Type: application/json' -d '{"title":"Java Fundamentals","capacity":2}'
```

Create learners (repeat with IDs `L2`, `L3`, etc.):

```bash
curl -X POST http://localhost:8080/api/learners -H 'Content-Type: application/json' -d '{"id":"L1","name":"Asha","prerequisiteCompleted":true}'
curl -X POST http://localhost:8080/api/learners -H 'Content-Type: application/json' -d '{"id":"L2","name":"Ravi","prerequisiteCompleted":true}'
curl -X POST http://localhost:8080/api/learners -H 'Content-Type: application/json' -d '{"id":"L3","name":"Maya","prerequisiteCompleted":true}'
```

Enroll L1, L2, then L3 (the third request should be waitlisted):

```bash
curl -X POST 'http://localhost:8080/api/courses/1/registrations?learnerId=L1'
curl -X POST 'http://localhost:8080/api/courses/1/registrations?learnerId=L2'
curl -X POST 'http://localhost:8080/api/courses/1/registrations?learnerId=L3'
```

Drop L1; L3 should be promoted:

```bash
curl -X DELETE http://localhost:8080/api/courses/1/registrations/L1
curl http://localhost:8080/api/courses/1/registrations
curl http://localhost:8080/api/courses/1
```

Inspect all registration records, including `DROPPED` and `SKIPPED` history:

```bash
curl http://localhost:8080/api/courses/1/registrations
```

Update a learner's prerequisite state to demonstrate promotion-time eligibility revalidation:

```bash
curl -X PATCH http://localhost:8080/api/learners/L3/prerequisite -H 'Content-Type: application/json' -d '{"prerequisiteCompleted":false}'
```

## API summary

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/courses` | Create an offering |
| GET | `/api/courses/{courseId}` | Read capacity and counts |
| GET | `/api/courses/{courseId}/registrations` | Read registration and queue history |
| POST | `/api/learners` | Create a learner |
| PATCH | `/api/learners/{learnerId}/prerequisite` | Update prerequisite completion |
| POST | `/api/courses/{courseId}/registrations?learnerId={id}` | Enroll or waitlist |
| DELETE | `/api/courses/{courseId}/registrations/{learnerId}` | Drop and promote |

## Tests

Run all tests with `mvn clean test`. The test suite covers available/full capacity, duplicate requests, prerequisite failure, FIFO promotion, skipping a learner who became ineligible, invalid course/learner, repeated drops, and concurrent enrollment attempts. The concurrency test asserts that enrolled count never exceeds capacity.

## Design and evidence

- `docs/DESIGN.md` — service boundary, model, APIs, invariants, transaction/locking strategy, trade-offs, and risks.
- `docs/AI_USAGE_RECORD.md` — representative prompt, AI contribution summary, accepted/edited decisions, and independent verification checklist.
- `docs/TEST_EVIDENCE.md` — test matrix and execution evidence status.

## Important implementation note

The assessment asks candidates to understand and work in a supplied starter project. No starter code was included with the assessment PDF in this workspace, so this project uses the explicitly permitted minimal service fallback. Review the code, run the tests locally, and update the AI usage record with the actual prompts and verification you personally perform before submission. Do not claim tests passed until you have run them in your environment.

## Externalized configuration, logging and exception handling

- H2 is the local default; MySQL profiles are provided for DEV/UAT/PROD.
- External profile configuration can be loaded from `./config/` without rebuilding the JAR.
- DB credentials must be injected from environment variables or a secrets manager.
- Logback writes console and rolling files; `LOG_DIR` and `LOG_LEVEL` are configurable.
- Each request receives an `X-Correlation-ID`, included in logs and error responses.
- Global exception handling returns consistent 4xx/5xx responses and does not leak internal details.
- Actuator health, info and metrics endpoints are enabled.

See `docs/CONFIGURATION_AND_LOGGING.md` for commands and operational guidance.

See `docs/MYSQL_SETUP.md` for MySQL database creation, connection configuration, TLS and migration guidance.

## Naming and package structure

The Maven artifact and Spring application are named `course-registration-service` because the service owns course-offering registration and waitlist workflows. The Java base package is `com.academy.courseregistration` (a neutral organization namespace for this assessment; replace `com.academy` with your employer or domain namespace in a production repository).

Key classes have domain- or responsibility-based names rather than placeholder names:

- `CourseOffering`: a specific scheduled instance of a course, including its capacity.
- `Learner`: a person eligible to register for an offering.
- `Registration`: the learner's registration state for an offering (`ENROLLED` or `WAITLISTED`).
- `RegistrationService`: coordinates enrollment, drop, prerequisite validation, and FIFO promotion as one transactional use case.
- `CourseOfferingController`, `LearnerController`, `RegistrationController`: expose resource-specific HTTP endpoints.
- `CourseOfferingRepository`, `LearnerRepository`, `RegistrationRepository`: isolate persistence access through Spring Data JPA.
- DTOs such as `CreateCourseOfferingRequest` and `RegistrationResponse`: define explicit API contracts without exposing JPA entities.
- `ApiExceptionHandler`: maps validation and business failures into consistent HTTP responses.
- `CorrelationIdFilter`: propagates a request correlation ID into logs and the response header.

The names are intentionally tied to the assessment's bounded domain. They are not generic sample names, and they avoid pretending this single service is a complete learning-management platform.
