# Distributed Rate Limiter

## Objective

Build a production-quality distributed rate limiter that can be used by multiple application instances and shares rate-limit state through Redis.

## Primary Language

Java.

## Storage

Redis.

## Initial Algorithm

Token Bucket.

## Goals

The final system should:

- Be thread-safe.
- Work across multiple application instances.
- Support configurable rate limits.
- Support burst capacity.
- Use Redis for distributed coordination.
- Provide an HTTP middleware.
- Have strong automated test coverage.
- Include benchmarks.
- Provide useful logging.
- Provide metrics.
- Handle Redis failures gracefully.
- Have Docker-based local development.
- Have clear documentation.

## Non-Goals

Do not build:

- A user-management system.
- A billing system.
- A frontend.
- A cloud-hosting platform.

Focus on the rate limiter.

## Engineering Priorities

1. Correctness
2. Concurrency safety
3. Distributed consistency
4. Testability
5. Performance
6. Observability
7. Documentation

## Success Criteria

A developer should be able to:

1. Clone the repository.
2. Start Redis.
3. Run the rate limiter.
4. Configure a rate limit.
5. Send requests through the middleware.
6. Observe requests being allowed/rejected.
7. Run the test suite.
8. Run benchmarks.
9. Understand the architecture from the documentation.