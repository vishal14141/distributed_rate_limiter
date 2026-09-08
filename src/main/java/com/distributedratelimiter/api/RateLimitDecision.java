package com.distributedratelimiter.api;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of a {@link RateLimiter#tryAcquire(int)} call.
 *
 * <p>Denied decisions include a non-negative {@link #retryAfter()} hint. Allowed decisions have an
 * empty retry-after.
 */
public final class RateLimitDecision {

    private final boolean allowed;
    private final long remainingTokens;
    private final Duration retryAfter;

    private RateLimitDecision(boolean allowed, long remainingTokens, Duration retryAfter) {
        if (remainingTokens < 0) {
            throw new IllegalArgumentException("remainingTokens must be >= 0");
        }
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
        this.retryAfter = Objects.requireNonNull(retryAfter, "retryAfter");
        if (retryAfter.isNegative()) {
            throw new IllegalArgumentException("retryAfter must not be negative");
        }
        if (allowed && !retryAfter.isZero()) {
            throw new IllegalArgumentException("allowed decisions must have a zero retryAfter");
        }
    }

    /** Allows the request; {@code remainingTokens} is the balance after consumption. */
    public static RateLimitDecision allow(long remainingTokens) {
        return new RateLimitDecision(true, remainingTokens, Duration.ZERO);
    }

    /**
     * Denies the request.
     *
     * @param remainingTokens tokens still in the bucket (often {@code 0})
     * @param retryAfter suggested wait before the next attempt; must not be negative
     */
    public static RateLimitDecision deny(long remainingTokens, Duration retryAfter) {
        return new RateLimitDecision(false, remainingTokens, retryAfter);
    }

    public boolean allowed() {
        return allowed;
    }

    public boolean denied() {
        return !allowed;
    }

    public long remainingTokens() {
        return remainingTokens;
    }

    /** Empty when the request was allowed. */
    public Optional<Duration> retryAfter() {
        if (allowed) {
            return Optional.empty();
        }
        return Optional.of(retryAfter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RateLimitDecision other)) {
            return false;
        }
        return allowed == other.allowed
                && remainingTokens == other.remainingTokens
                && retryAfter.equals(other.retryAfter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowed, remainingTokens, retryAfter);
    }

    @Override
    public String toString() {
        return "RateLimitDecision{allowed="
                + allowed
                + ", remainingTokens="
                + remainingTokens
                + ", retryAfter="
                + retryAfter
                + '}';
    }
}
