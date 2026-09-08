package com.distributedratelimiter.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Token-bucket parameters: maximum burst size and refill cadence.
 *
 * <p>Refill is expressed as {@code refillTokens} added every {@code refillPeriod}. For example,
 * capacity 100 and 10 tokens per second is {@code of(100, 10, Duration.ofSeconds(1))}.
 */
public final class RateLimitConfig {

    private final long capacity;
    private final long refillTokens;
    private final Duration refillPeriod;

    private RateLimitConfig(long capacity, long refillTokens, Duration refillPeriod) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        if (refillTokens < 1) {
            throw new IllegalArgumentException("refillTokens must be at least 1");
        }
        Objects.requireNonNull(refillPeriod, "refillPeriod");
        if (refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("refillPeriod must be positive");
        }
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriod = refillPeriod;
    }

    /**
     * Creates a validated configuration.
     *
     * @param capacity maximum tokens the bucket may hold (burst size); must be at least 1
     * @param refillTokens tokens added each period; must be at least 1
     * @param refillPeriod time between refills; must be positive
     */
    public static RateLimitConfig of(long capacity, long refillTokens, Duration refillPeriod) {
        return new RateLimitConfig(capacity, refillTokens, refillPeriod);
    }

    /**
     * Creates a configuration that adds {@code tokensPerSecond} tokens every second, up to {@code
     * capacity}.
     */
    public static RateLimitConfig perSecond(long capacity, long tokensPerSecond) {
        return of(capacity, tokensPerSecond, Duration.ofSeconds(1));
    }

    public long capacity() {
        return capacity;
    }

    public long refillTokens() {
        return refillTokens;
    }

    public Duration refillPeriod() {
        return refillPeriod;
    }

    /**
     * Average refill rate in tokens per second. Used by later algorithm work; not a substitute for
     * the exact {@link #refillTokens()} / {@link #refillPeriod()} pair.
     */
    public double tokensPerSecond() {
        double periodSeconds = refillPeriod.toNanos() / 1_000_000_000.0;
        return refillTokens / periodSeconds;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RateLimitConfig other)) {
            return false;
        }
        return capacity == other.capacity
                && refillTokens == other.refillTokens
                && refillPeriod.equals(other.refillPeriod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capacity, refillTokens, refillPeriod);
    }

    @Override
    public String toString() {
        return "RateLimitConfig{capacity="
                + capacity
                + ", refillTokens="
                + refillTokens
                + ", refillPeriod="
                + refillPeriod
                + '}';
    }
}
