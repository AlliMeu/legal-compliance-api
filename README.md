# legal-compliance-api

![CI](https://github.com/AlliMeu/legal-compliance-api/actions/workflows/ci.yml/badge.svg)

A small Spring Boot REST API for managing legal case files under regulatory
compliance rules - built as a deliberate testing exercise, not a real product.

## Why this exists

I wanted a project to practice five testing skills at once: error handling,
edge cases on a small pure function, Mockito verification of a service,
a date-range calculation with missing data, and Spring Security authorization.
I picked a domain I know from my previous career, legal case compliance.

## What it demonstrates

| Area | Where | What's being tested |
|---|---|---|
| Error responses | `exception/GlobalExceptionHandler.java` | A business-rule rejection, an unknown case (404), an unreadable JSON body and Bean Validation failures each return a JSON error with a clear `errorId`. Errors Spring already handles (405, 415, unknown path) keep Spring's own status. |
| Edge-case unit testing | `util/CaseReferenceMasker.java` | A case reference is masked before it reaches a log line: the first 3 characters stay visible, a reference of 3 characters or less is fully masked. Tested with long, short, empty and null input. |
| Service with a repository | `audit/CaseAccessAuditService.java` | Every access to a case file is saved as one log entry (case, user, category, channel, time). Tests check that an unknown case is rejected with a 404 and nothing is saved, and that a valid access saves exactly the expected entry (checked with an `ArgumentCaptor`). |
| Date range with missing data | `compliance/RetentionPeriodCalculator.java` | A case can be subject to several retention rules at once, each covering only part of the timeline; the real retention window is the earliest start any rule states through the latest end any rule states. Exposed as `POST /compliance/retention-period`. Contradictory rules (start after end) are rejected with a 400. Tested with all-bounds-present, missing-start-everywhere, missing-end-everywhere, mixed-missing-values, contradictory and null inputs. |
| Role-based authorization | `config/SecurityConfig.java` | `GET /cases/**` needs `SCOPE_case:read`, `POST /cases/{ref}/access` needs `SCOPE_case:write`. Tested for wrong scope (403), no authentication and wrong password (401), and correct scope (success) via `MockMvc`, with the real demo users, and again over real HTTP via Karate. |

Two test layers on purpose: `MockMvc`-based tests for fast unit/slice-level
feedback, and a Karate suite (`src/test/resources/.../karate/cases.feature`)
that drives the same endpoints as a black-box HTTP client against a running
Spring context, with the database mocked. The `docker-e2e-smoke` CI job then
checks key responses against the real stack (app + MongoDB, no mocks).

## Stack

Java 17, Spring Boot 3, Spring Security, Spring Data MongoDB, JUnit 5,
Mockito, AssertJ, Karate, Docker.

## Running it

```bash
docker compose up --build
```

Starts the API on `:8080` with a real MongoDB. Two demo users are seeded in
`SecurityConfig` for manual testing (Basic auth): `paralegal` / `demo`
(read-only) and `attorney` / `demo` (read + write).

```bash
curl -u attorney:demo http://localhost:8080/cases/REF-001
```

The database starts empty (no case is seeded), so the expected answer is a
clean JSON 404 from the error handler, not a stack trace:

```json
{"errorId":"NOT_FOUND","message":"No case found for reference REF-001","location":null}
```

## Running the tests

```bash
mvn test
```

Requires **JDK 17** specifically to run - Karate's embedded GraalJS engine
isn't yet compatible with newer JDKs (`sun.misc.Unsafe.ensureClassInitialized`
was removed). If `mvn test` fails with a `GraalVM`/`Unsafe` error, point
`JAVA_HOME` at a JDK 17 install and retry. CI is pinned to JDK 17 for the same
reason.

## Notes on the domain

Cases, matters, and retention rules in this repo are all invented for the
purpose of the exercise - no real client data, case names, or regulatory
citations are used anywhere.
