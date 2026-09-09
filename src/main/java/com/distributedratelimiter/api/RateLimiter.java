package com.distributedratelimiter.api;

import com.distributedratelimiter.config.RateLimitConfig;

/**
 * Decides whether a caller may consume tokens under a configured limit.
 *
 * <p>Implementations must be safe for concurrent callers. The in-memory backend serializes acquires
 * per key; Redis-backed implementations are added in later days. This type is the stable port
 * applications and HTTP middleware should call.
 */
public interface RateLimiter {

    /** Bucket key used by {@link #tryAcquire()} when the caller does not supply one. */
    String DEFAULT_KEY = "default";

    /** Limit applied by this limiter. */
    RateLimitConfig config();

    /**
     * Attempts to consume {@code request.permits()} tokens from the bucket identified by {@code
     * request.key()}.
     */
    RateLimitDecision tryAcquire(RateLimitRequest request);

    default RateLimitDecision tryAcquire(String key, long permits) {
        return tryAcquire(new RateLimitRequest(key, permits));
    }

    default RateLimitDecision tryAcquire(String key) {
        return tryAcquire(RateLimitRequest.onePermit(key));
    }

    default RateLimitDecision tryAcquire() {
        return tryAcquire(DEFAULT_KEY);
    }
}
