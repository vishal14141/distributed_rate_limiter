package com.distributedratelimiter.core;

import com.distributedratelimiter.api.RateLimitDecision;
import com.distributedratelimiter.api.RateLimitRequest;
import com.distributedratelimiter.api.RateLimiter;
import com.distributedratelimiter.config.RateLimitConfig;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token-bucket {@link RateLimiter} that keeps per-key buckets in process memory.
 *
 * <p>Each key has its own bucket starting at {@link RateLimitConfig#capacity()}. Tokens refill
 * continuously at {@code refillTokens} per {@code refillPeriod} and never exceed capacity. This
 * backend is not shared across JVMs.
 *
 * <p>The limiter is safe for concurrent use: buckets are stored in a {@link ConcurrentHashMap}, and
 * each bucket serializes {@link #tryAcquire} so two threads cannot spend the same tokens. Distinct
 * keys do not share a lock, so they can be acquired in parallel.
 */
public final class InMemoryRateLimiter implements RateLimiter {

    private final RateLimitConfig config;
    private final Clock clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(RateLimitConfig config) {
        this(config, Clock.systemUTC());
    }

    public InMemoryRateLimiter(RateLimitConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config, "config");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public RateLimitConfig config() {
        return config;
    }

    @Override
    public RateLimitDecision tryAcquire(RateLimitRequest request) {
        Objects.requireNonNull(request, "request");
        // computeIfAbsent publishes one Bucket per key; tryAcquire then locks that instance.
        Bucket bucket = buckets.computeIfAbsent(request.key(), ignored -> new Bucket());
        return bucket.tryAcquire(request);
    }

    private final class Bucket {

        private double tokens;
        private long lastRefillNanos;
        private boolean initialized;

        /**
         * Exclusive mutation of this key's token balance. Callers must not invoke refill or
         * remaining-token helpers except through this method.
         */
        synchronized RateLimitDecision tryAcquire(RateLimitRequest request) {
            refill(nowNanos());
            long permits = request.permits();
            if (tokens >= permits) {
                tokens -= permits;
                return RateLimitDecision.allow(request.key(), remainingTokens());
            }
            return RateLimitDecision.deny(request.key(), remainingTokens(), retryAfter(permits));
        }

        private void refill(long nowNanos) {
            if (!initialized) {
                tokens = config.capacity();
                lastRefillNanos = nowNanos;
                initialized = true;
                return;
            }
            long elapsed = nowNanos - lastRefillNanos;
            if (elapsed <= 0) {
                return;
            }
            double added = elapsed * (double) config.refillTokens() / periodNanos();
            tokens = Math.min(config.capacity(), tokens + added);
            lastRefillNanos = nowNanos;
        }

        private Duration retryAfter(long permits) {
            if (permits > config.capacity()) {
                return Duration.ZERO;
            }
            double missing = permits - tokens;
            if (missing <= 0) {
                return Duration.ZERO;
            }
            double waitNanos = missing * periodNanos() / config.refillTokens();
            long nanos = (long) Math.ceil(waitNanos);
            if (nanos < 1L) {
                nanos = 1L;
            }
            return Duration.ofNanos(nanos);
        }

        private long remainingTokens() {
            return (long) Math.floor(tokens);
        }

        private long periodNanos() {
            long nanos = config.refillPeriod().toNanos();
            if (nanos <= 0L) {
                throw new IllegalStateException("refillPeriod resolved to a non-positive duration");
            }
            return nanos;
        }

        private long nowNanos() {
            Instant now = clock.instant();
            return Math.multiplyExact(now.getEpochSecond(), 1_000_000_000L) + now.getNano();
        }
    }
}
