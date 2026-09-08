package com.distributedratelimiter.config;

import java.util.Objects;

/**
 * Named rate-limit policy. Multiple policies will be selectable later; Day 2 only defines the value
 * type.
 */
public record RateLimitPolicy(String name, RateLimitConfig config) {

    public RateLimitPolicy {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(config, "config");
    }
}
