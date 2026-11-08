# Test Evidence

## Test matrix

| Acceptance area | Test | Expected evidence |
|---|---|---|
| Available capacity | `enrollsWhileCapacityRemainsThenQueuesWhenFull` | First two enroll; third waitlists; counts remain correct |
| Duplicate enrollment | `duplicateRequestDoesNotCreateAnotherRecord` | Same registration returned; row count remains one |
| Duplicate waitlist request | `duplicateWaitlistRequestDoesNotCreateAnotherRecord` | One waitlist row for learner |
| Prerequisite failure | `rejectsLearnerWhoHasNotCompletedPrerequisite` and unit test | Business rule error; no row persisted |
| FIFO promotion | `droppingEnrollmentPromotesEarliestEligibleWaitlistedLearner` | Earliest waitlisted learner becomes enrolled |
| Ineligible candidate | `skipsLearnerWhosePrerequisiteBecomesIncompleteAndPromotesNextEligible` | Candidate becomes SKIPPED; next eligible becomes enrolled |
| Invalid requests | `invalidLearnerAndCourseAreRejected` | Business errors; no corrupted state |
| Repeated drop | `repeatedDropDoesNotReleaseAnotherSeat` | Second drop rejected; enrolled count stays zero |
| Concurrent requests | `concurrentEnrollmentsNeverExceedCourseCapacity` | Parallel requests do not exceed capacity |

## Actual execution result

**Not executed in the generation environment.** Maven was not installed. Run from the project root:

```bash
mvn clean test
```

Paste the actual Maven/Surefire summary here after running it. Do not mark the suite as passed without a successful local run.

## Manual API verification

Follow the “Quick API walkthrough” in `README.md`. Record the actual HTTP status and response for course creation, learner creation, two enrollments, one waitlist entry, and a drop that promotes the first waitlisted learner.
