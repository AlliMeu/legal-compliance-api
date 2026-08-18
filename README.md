# legal-compliance-api

![CI](https://github.com/AlliMeu/legal-compliance-api/actions/workflows/ci.yml/badge.svg)

A small Spring Boot REST API for managing legal case files under regulatory
compliance rules - built as a deliberate testing exercise, not a real product.

## Why this exists

I wanted a project that let me practice five specific QA/testing skills at
once: exception-handling coverage, edge-case testing on a small pure function,
Mockito verification of a service with conditional branches and a database
upsert, testing a date-range calculation with missing data, and Spring
Security authorization testing. Rather than another CRUD-with-happy-path-only
demo, I picked a domain - legal case compliance - that's close to where I'm
job-hunting (LegalTech) and lets the design choices below be things I
genuinely thought about, not boilerplate.

## What it demonstrates

| Area | Where | What's being tested |
|---|---|---|
| Exception -> HTTP status mapping | `exception/GlobalExceptionHandler.java` | Every branch: a business-rule rejection, a not-found case, a malformed JSON body (including an invalid enum value), Bean Validation failures, and an unexpected-error fallback - each asserted on exact status code and error shape, not just "didn't throw." |
| Edge-case unit testing | `util/CaseReferenceMasker.java` | A case reference gets masked before it ever reaches a log line - the same instinct as a privilege log, just automated. Tested against long, exactly-boundary-length, short, empty, and null input. |
| Service with conditional branches + DB upsert | `audit/CaseAccessAuditServiceImpl.java` | Records who accessed a case file and when. Tests prove: disabled auditing short-circuits before any DB call, an unknown case reference is looked up but never written, a normal access builds the expected Mongo update (verified via `ArgumentCaptor`, not just "was called"), and a duplicate-key conflict falls back to updating the existing record instead of failing. |
| Date range with missing data | `compliance/RetentionPeriodCalculator.java` | A case can be subject to several retention rules at once, each covering only part of the timeline; the real retention window is the earliest start any rule states through the latest end any rule states. Tested with all-bounds-present, missing-start-everywhere, missing-end-everywhere, and mixed-missing-values cases. |
| Role-based authorization | `config/SecurityConfig.java` | `GET /cases/**` needs `SCOPE_case:read`, `POST /cases/{ref}/access` needs `SCOPE_case:write`. Tested for wrong-scope (403), no authentication (401), and correct scope (success) via `@WithMockUser` + `MockMvc`, and again end-to-end over real HTTP via Karate. |

Two test layers on purpose: `MockMvc`-based tests for fast unit/slice-level
feedback, and a Karate suite (`src/test/resources/.../karate/cases.feature`)
that drives the same endpoints as a real black-box HTTP client against a
running Spring context - the API contract proven from both sides.

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
