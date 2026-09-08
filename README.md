# Distributed Rate Limiter

A Java library for token-bucket rate limiting with Redis-backed shared state. This repository is in early development; Day 2 adds public limiter interfaces and configuration types. Token-bucket enforcement is not implemented yet.

## Requirements

- JDK 17 or newer

Maven is not required; use the included wrapper (`./mvnw`).

## Build

```bash
./mvnw verify
```

Format sources:

```bash
./mvnw spotless:apply
```

## Layout

- `src/main/java` — library sources (`com.distributedratelimiter.api`, `.config`)
- `src/test/java` — unit tests
- `docs/ARCHITECTURE.md` — component boundaries and design decisions
- `PROJECT.md` — goals and non-goals
- `ROADMAP.md` — 30-day plan
- `AGENTS.md` — development process

## Status

Public `RateLimiter` API and immutable rate-limit configuration types. Algorithms, Redis, and HTTP middleware are not implemented yet.
