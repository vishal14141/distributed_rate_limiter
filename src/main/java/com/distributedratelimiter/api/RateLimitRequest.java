package com.distributedratelimiter.api;

import java.util.Objects;

/**
 * A request to consume tokens from a named bucket.
 *
 * @param key bucket identity (for example a client or route); must not be blank
 * @param permits tokens to consume; must be positive
 */
public record RateLimitRequest(String key, long permits) {

    public RateLimitRequest {
        Objects.requireNonNull(key, "key");
        if (key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive, got " + permits);
        }
    }

    public static RateLimitRequest onePermit(String key) {
        return new RateLimitRequest(key, 1L);
    }
}
