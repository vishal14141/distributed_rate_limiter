# Distributed Rate Limiter

A Java library for token-bucket rate limiting with Redis-backed shared state. This repository is in early development; Day 2 adds the public limiter API and configuration types. Token-bucket behavior is not implemented yet.

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

- `src/main/java` — library sources
  - `com.distributedratelimiter` — library identity
  - `com.distributedratelimiter.api` — `RateLimiter` and `RateLimitDecision`
  - `com.distributedratelimiter.config` — `RateLimitConfig`
- `src/test/java` — unit tests
- `docs/ARCHITECTURE.md` — component boundaries and design decisions
- `PROJECT.md` — goals and non-goals
- `ROADMAP.md` — 30-day plan
- `AGENTS.md` — development process

## Status

Public interfaces and configuration types are in place. In-memory token bucket, Redis, and HTTP middleware are not implemented yet.

Example configuration (no limiter implementation yet):

```java
import com.distributedratelimiter.api.RateLimitDecision;
import com.distributedratelimiter.config.RateLimitConfig;
import java.time.Duration;

RateLimitConfig config = RateLimitConfig.perSecond(100, 10);
RateLimitDecision denied = RateLimitDecision.deny(0, Duration.ofMillis(100));
```
