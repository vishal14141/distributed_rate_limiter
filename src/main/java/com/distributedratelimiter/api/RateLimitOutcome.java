package com.distributedratelimiter.api;

/** Result of evaluating a rate-limit request. Storage failures are a later concern. */
public enum RateLimitOutcome {
    ALLOWED,
    DENIED
}
