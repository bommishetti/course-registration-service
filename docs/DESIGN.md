# Design Specification — Course Registration and Waitlist

## 1. Scope

This service implements the bounded workflow in the assessment: one course offering's learner enrollment, dropping, and FIFO waitlist promotion. It is a REST service with a relational persistence model. It does not implement a full registration product.

## 2. Service boundary and components

- **CourseOfferingController** creates and reads course offerings and exposes registration history.
- **LearnerController** creates learners and updates their prerequisite-completion state for the demonstration.
- **RegistrationController** accepts enrollment and drop commands.
- **RegistrationService** owns business rules and transaction boundaries.
- **Repositories** encapsulate JPA persistence. `CourseOfferingRepository.findByIdForUpdate` obtains a pessimistic write lock on the course row.
- **ApiExceptionHandler** converts expected business and validation failures into structured HTTP errors.

Request flow: HTTP controller → RegistrationService → lock course row → validate learner and existing registration → count seats / update registration → commit transaction.

## 3. Data model

### CourseOffering

- `id`: generated identifier
- `title`: required course title
- `capacity`: positive maximum enrolled count
- `version`: optimistic version metadata

### Learner

- `id`: caller-provided learner identifier
- `name`: required display name
- `prerequisiteCompleted`: current eligibility fact for this bounded example

### Registration

- `id`: generated identifier
- `course_id`, `learner_id`: references to offering and learner
- `status`: `ENROLLED`, `WAITLISTED`, `DROPPED`, or `SKIPPED`
- `created_at`: immutable queue-order timestamp
- `updated_at`: last status-change timestamp

A database unique constraint on `(course_id, learner_id)` prevents duplicate registration rows even if application-level checks are bypassed. The waitlist query sorts by `created_at ASC, id ASC` to make FIFO deterministic.

## 4. API design

| Method | Path | Request / response |
|---|---|---|
| POST | `/api/courses` | `{title, capacity}` → course summary |
| GET | `/api/courses/{courseId}` | course summary with enrolled/waitlist counts |
| GET | `/api/courses/{courseId}/registrations` | ordered registration history |
| POST | `/api/learners` | `{id, name, prerequisiteCompleted}` → learner |
| PATCH | `/api/learners/{learnerId}/prerequisite` | `{prerequisiteCompleted}` → updated learner |
| POST | `/api/courses/{courseId}/registrations?learnerId={id}` | enroll or waitlist result |
| DELETE | `/api/courses/{courseId}/registrations/{learnerId}` | drop result; promotion occurs in same transaction |

Success responses expose the resulting status and a human-readable message. Errors use `{code, message, timestamp}`. Missing course/learner/registration returns a not-found error; invalid state transitions and unmet prerequisites return conflict errors; malformed request data returns a validation/bad-request error.

## 5. Business rules and assumptions

1. Prerequisite completion is represented by a boolean on Learner; the assessment does not prescribe a prerequisite schema.
2. A learner without the prerequisite cannot enroll or enter the waitlist.
3. A learner has one registration row per offering. Duplicate enrollment attempts return the existing state and do not create a second row. A learner whose registration is `DROPPED` or `SKIPPED` cannot register again for that offering. This interprets “a learner may enroll only once” strictly.
4. If there is capacity, a valid learner becomes `ENROLLED`; otherwise they become `WAITLISTED`.
5. On drop, the earliest currently waitlisted eligible learner is promoted. If a learner at the head has since become ineligible, mark them `SKIPPED` and continue scanning. This skip policy is explicitly documented and consistently applied at promotion time.
6. One seat is opened by a valid drop and at most one eligible waitlisted learner is promoted.
7. The waitlist is per offering, not global.

## 6. Invariants

- `0 <= enrolledCount <= capacity`.
- At most one registration row exists for a `(course, learner)` pair.
- A registration is in exactly one status at a time.
- Only learners with a completed prerequisite may be `ENROLLED` or newly `WAITLISTED`.
- Promotion selects the minimum `(createdAt, id)` among currently eligible `WAITLISTED` rows.
- A drop only changes an `ENROLLED` registration and cannot release a second seat.
- A failed request does not partially mutate enrollment or queue state.

## 7. Transaction and concurrency strategy

Enrollment and drop are `@Transactional`. Both first acquire a pessimistic write lock on the same course-offering row (`SELECT ... FOR UPDATE`). This serializes all state-changing requests for that offering, so the capacity check and registration state change occur as one critical section. A second request for the same course waits for the first transaction to finish, then observes the committed enrollment count. Different offerings use different locks.

A unique database constraint on `(course_id, learner_id)` is a second line of defense against duplicate records. The concurrency integration test submits simultaneous enrollments and asserts the enrolled count remains at capacity.

For a production deployment, validate lock behavior against the chosen database and isolation level, configure lock timeouts/retry policy, and consider a transactional outbox if promotion must emit external notifications. H2 is used here for a portable local demonstration, not as a production database recommendation.

## 8. Testing strategy

The integration test suite uses the Spring application context and H2 to exercise persistence and transaction behavior. It covers available and full capacity, duplicate enrolled and waitlisted requests, prerequisite failure, FIFO promotion, skipping an ineligible head entry, invalid course/learner, repeated drop, and concurrent enrollments. A Mockito unit test verifies prerequisite failure does not persist a registration.

## 9. Trade-offs and limitations

- One offering and simple boolean prerequisite only; no prerequisite graph or policy engine.
- H2 in-memory persistence resets on restart; no migration framework or production database configuration.
- No authentication/authorization or learner ownership checks.
- No pagination, rate limiting, audit actor, observability, or notification delivery.
- Pessimistic row locking is simple and correct for this narrow workflow but may reduce throughput for a very popular single offering.
- `createdAt` uses application time; production systems with multiple application nodes should consider database-generated timestamps or a monotonic queue sequence.
- The unique registration row means dropped/skipped learners cannot re-enroll. If the business later permits re-enrollment, model attempts separately or introduce an explicit lifecycle policy.
- Course capacity is immutable through the exposed API; capacity reduction policy is outside scope.
