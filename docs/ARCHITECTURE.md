# Architecture

This document records the Day 1 design for the distributed rate limiter. It describes intended boundaries, not implemented features.

## Purpose

Provide a Java library that multiple application instances can use to enforce a shared token-bucket rate limit. Shared state will live in Redis so independent processes agree on remaining capacity.

## Design principles

1. **Correctness first.** Prefer a simple, testable algorithm over clever shortcuts.
2. **Small public surface.** Callers should depend on a few stable types, not storage or HTTP details.
3. **Replaceable storage.** An in-memory backend will exist for local tests; Redis will be the distributed backend.
4. **Fail visibly.** Redis and timeout failures will be explicit, not silent allow/deny without policy.
5. **No extra products.** This is not a user, billing, or frontend system.

## Intended components

```text
  Application / HTTP middleware
              |
              v
     RateLimiter (API)
              |
     +--------+--------+
     |                 |
     v                 v
 In-memory store    Redis store
 (tests / local)    (shared state)
```

| Component | Responsibility | Introduced |
| --- | --- | --- |
| API | Allow/deny decisions, configuration types | Day 2 |
| In-memory token bucket | Single-process algorithm and concurrency | Days 3–5 |
| HTTP middleware | Apply limits to requests | Day 6 |
| Redis store | Shared bucket state across instances | Days 8–13 |
| Observability | Logs, metrics, health | Days 19–21 |

## Target package layout

Packages will be added when the matching work starts. Day 1 only creates the root package.

| Package | Role |
| --- | --- |
| `com.distributedratelimiter` | Library identity (this change) |
| `com.distributedratelimiter.api` | Public limiter and decision types |
| `com.distributedratelimiter.core` | Token-bucket implementation |
| `com.distributedratelimiter.config` | Limit policies and loading |
| `com.distributedratelimiter.store` | Persistence ports and Redis adapter |
| `com.distributedratelimiter.http` | Servlet/filter or similar middleware |
| `com.distributedratelimiter.observe` | Logging and metrics hooks |

## Build and runtime baseline

| Choice | Decision | Reason |
| --- | --- | --- |
| Language | Java 17 | LTS, available locally, sufficient language features |
| Build | Maven with wrapper | Reproducible builds without a pre-installed Maven |
| Tests | JUnit 5 | Standard for JVM unit tests |
| Format | Spotless + Google Java Format (AOSP) | Consistent Java and POM formatting |
| Storage (later) | Redis | Required by the project for shared state |
| Algorithm (later) | Token bucket | Required initial algorithm |

## Non-goals for this phase

Do not implement token-bucket logic, Redis clients, HTTP middleware, configuration loading, metrics, or Docker in Day 1. Those follow the roadmap after this skeleton compiles and tests cleanly.

## Verification

A developer should be able to clone the repository, run `./mvnw test`, and see a passing smoke test that the library module exists.
