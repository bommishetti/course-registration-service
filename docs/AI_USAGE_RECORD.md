# AI-Assisted Engineering Use Record

> Candidate completion note: This record documents the AI assistance used to generate this starter implementation. Before submission, replace or supplement it with the exact prompts and excerpts from your own tool history, and record the tests you personally ran. Do not state that generated code is independently verified until you have performed that review.

## Engineering activity

AI was used to help decompose the assessment, propose a bounded Spring Boot design, generate an initial implementation, and draft focused tests and engineering documentation.

## Prompt record (summary of the request used)

**Prompt:** “Create the entire project from the attached AI-Assisted Engineering Assessment and provide it as a ZIP. Cover all the requirements.”

**Assessment constraints supplied to the AI:** implement enrollment, dropping and FIFO waitlist promotion for one offering; state prerequisite and ineligible-waitlist assumptions; cover duplicate/invalid requests and capacity; add focused tests; include design specification, AI-use record, run instructions, and limitations. Use a minimal service/CLI fallback if no starter is provided.

## AI response / contribution summary

The generated project includes Spring Boot REST endpoints, JPA entities and repositories, transactional registration logic, pessimistic course-row locking, a unique `(course_id, learner_id)` constraint, H2 configuration, JUnit/Mockito tests, a design specification, README, and this record.

## Decision log

| AI suggestion / generated choice | Decision | Reason / candidate review required |
|---|---|---|
| Use Spring Boot, Java 17, Maven | Accepted as implementation baseline | Fits a Java backend assessment and remains a small service |
| Use H2 in-memory database | Accepted | Assessment permits a minimal prototype; easy to run locally |
| Lock the course row pessimistically for enroll/drop | Accepted, subject to verification | Serializes capacity-changing operations for the same offering |
| Add unique constraint for course + learner | Accepted | Database-level duplicate protection |
| Skip an ineligible waitlisted learner and inspect the next | Accepted as explicit policy | Assessment asks candidate to state and consistently apply one policy |
| Keep DROPPED/SKIPPED registration history and prohibit re-registration | Accepted as a strict interpretation | Aligns with “a learner may enroll only once”; confirm this assumption with assessor if ambiguous |
| Add a prerequisite update endpoint | Accepted for demonstration/testing | Allows promotion-time eligibility changes to be exercised without a separate admin subsystem |
| Add concurrency test | Accepted | Verifies capacity invariant under parallel enrollment attempts |

## Verification evidence and independent review

The source includes tests for:

- available seats and full capacity;
- duplicate enrollment and duplicate waitlist requests;
- unmet prerequisite;
- FIFO promotion;
- skipping a waitlisted learner who becomes ineligible;
- invalid course/learner and repeated drop;
- concurrent enrollment attempts with a capacity assertion.

**Execution status in the generation environment:** not run. A JDK was available, but Maven was not installed in the environment. No claim is made here that `mvn clean test` passed.

**Candidate must complete before submission:**

1. Run `mvn clean test` on a machine with JDK 17+ and Maven 3.9+; paste the actual result into `docs/TEST_EVIDENCE.md`.
2. Read every generated source file and verify the transaction, locking, and error behavior.
3. Run the API walkthrough in the README and capture actual responses.
4. Check the exact Spring Boot/H2 behavior in the candidate's runtime environment.
5. Update this log with exact prompts, accepted/edited/rejected AI outputs, and the files changed after review.

## Files informed

`pom.xml`, `src/main/java/**`, `src/test/java/**`, `README.md`, `docs/DESIGN.md`, and `docs/TEST_EVIDENCE.md`.
