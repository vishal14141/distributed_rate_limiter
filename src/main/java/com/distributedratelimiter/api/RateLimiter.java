package com.distributedratelimiter.api;

/**
 * Decides whether a caller may consume tokens from a rate-limit bucket.
 *
 * <p>Implementations must be thread-safe. Storage (in-memory or Redis) is an implementation concern
 * and is not part of this interface.
 */
public interface RateLimiter {

    /**
     * Attempts to consume a single token.
     *
     * @return an allow or deny decision; never {@code null}
     */
    default RateLimitDecision tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * Attempts to consume {@code tokens} from the bucket.
     *
     * @param tokens number of tokens to consume; must be at least 1
     * @return an allow or deny decision; never {@code null}
     * @throws IllegalArgumentException if {@code tokens} is less than 1
     */
    RateLimitDecision tryAcquire(int tokens);
}
