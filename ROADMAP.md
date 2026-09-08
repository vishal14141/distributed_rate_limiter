# 30-Day Development Roadmap

## Phase 1 — Foundation

### Day 1
Project structure and architecture. **Done** — Maven wrapper, Java 17 module, architecture doc, smoke test.

### Day 2
Basic rate limiter interfaces and configuration. **Done** — `RateLimiter`, `RateLimitDecision`, and `RateLimitConfig` with validation tests.

### Day 3
Implement in-memory token bucket.

### Day 4
Unit tests for token bucket behavior.

### Day 5
Concurrency safety and race detection.

### Day 6
HTTP middleware.

### Day 7
Documentation and refactoring.

## Phase 2 — Distributed System

### Day 8
Redis integration.

### Day 9
Redis-backed token bucket.

### Day 10
Distributed request coordination.

### Day 11
Concurrent distributed requests.

### Day 12
Redis failure handling.

### Day 13
Timeouts and connection management.

### Day 14
Integration tests.

## Phase 3 — Production Features

### Day 15
Configuration system.

### Day 16
Multiple rate-limit policies.

### Day 17
Per-user/per-key rate limiting.

### Day 18
HTTP response headers.

### Day 19
Structured logging.

### Day 20
Metrics.

### Day 21
Health checks.

## Phase 4 — Performance

### Day 22
Benchmark suite.

### Day 23
Performance analysis.

### Day 24
Reduce unnecessary allocations.

### Day 25
Redis performance improvements.

### Day 26
Load testing.

## Phase 5 — Hardening

### Day 27
Failure scenarios.

### Day 28
Security review.

### Day 29
Architecture review and cleanup.

### Day 30
Final tests, benchmarks, documentation and project review.