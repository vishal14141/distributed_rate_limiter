package com.distributedratelimiter.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Token-bucket parameters for a single limit: burst capacity and refill pace.
 *
 * <p>{@code capacity} is the maximum tokens the bucket may hold (burst). {@code refillTokens} are
 * added every {@code refillPeriod}. Neither storage nor refill math is performed here.
 */
public record RateLimitConfig(long capacity, long refillTokens, Duration refillPeriod) {

    /**
     * @param capacity maximum tokens in the bucket; must be positive
     * @param refillTokens tokens added each period; must be positive
     * @param refillPeriod time between refills; must be positive
     */
    public RateLimitConfig {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, got " + capacity);
        }
        if (refillTokens <= 0) {
            throw new IllegalArgumentException(
                    "refillTokens must be positive, got " + refillTokens);
        }
        Objects.requireNonNull(refillPeriod, "refillPeriod");
        if (refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException(
                    "refillPeriod must be positive, got " + refillPeriod);
        }
    }

    /** Burst size equal to {@link #capacity()}. */
    public long burstCapacity() {
        return capacity;
    }

    /** Convenience for {@code refillTokens} tokens every second with the given burst capacity. */
    public static RateLimitConfig tokensPerSecond(long capacity, long tokensPerSecond) {
        return new RateLimitConfig(capacity, tokensPerSecond, Duration.ofSeconds(1));
    }
}
