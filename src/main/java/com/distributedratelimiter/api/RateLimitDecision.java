package com.distributedratelimiter.api;

import java.time.Duration;
import java.util.Objects;

/**
 * Allow or deny outcome for one {@link RateLimitRequest}.
 *
 * @param outcome whether the request may proceed
 * @param key bucket that was evaluated
 * @param remainingTokens estimated tokens left after the decision; never negative
 * @param retryAfter wait suggested before retrying; {@link Duration#ZERO} when allowed
 */
public record RateLimitDecision(
        RateLimitOutcome outcome, String key, long remainingTokens, Duration retryAfter) {

    public RateLimitDecision {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(key, "key");
        if (key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (remainingTokens < 0) {
            throw new IllegalArgumentException(
                    "remainingTokens must not be negative, got " + remainingTokens);
        }
        Objects.requireNonNull(retryAfter, "retryAfter");
        if (retryAfter.isNegative()) {
            throw new IllegalArgumentException(
                    "retryAfter must not be negative, got " + retryAfter);
        }
    }

    public boolean allowed() {
        return outcome == RateLimitOutcome.ALLOWED;
    }

    public static RateLimitDecision allow(String key, long remainingTokens) {
        return new RateLimitDecision(RateLimitOutcome.ALLOWED, key, remainingTokens, Duration.ZERO);
    }

    public static RateLimitDecision deny(String key, long remainingTokens, Duration retryAfter) {
        return new RateLimitDecision(RateLimitOutcome.DENIED, key, remainingTokens, retryAfter);
    }
}
