package com.distributedratelimiter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.distributedratelimiter.api.RateLimitDecision;
import com.distributedratelimiter.api.RateLimitRequest;
import com.distributedratelimiter.config.RateLimitConfig;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class InMemoryRateLimiterTest {

    @Test
    void allowsUpToCapacityThenDenies() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(3, 1), clock);

        assertTrue(limiter.tryAcquire("client").allowed());
        assertTrue(limiter.tryAcquire("client").allowed());
        RateLimitDecision lastAllow = limiter.tryAcquire("client");
        assertTrue(lastAllow.allowed());
        assertEquals(0L, lastAllow.remainingTokens());

        RateLimitDecision denied = limiter.tryAcquire("client");
        assertFalse(denied.allowed());
        assertEquals(0L, denied.remainingTokens());
        assertTrue(denied.retryAfter().compareTo(Duration.ZERO) > 0);
    }

    @Test
    void refillsTokensAsTimePasses() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(new RateLimitConfig(2, 2, Duration.ofSeconds(1)), clock);

        assertTrue(limiter.tryAcquire("k").allowed());
        assertTrue(limiter.tryAcquire("k").allowed());
        assertFalse(limiter.tryAcquire("k").allowed());

        clock.advance(Duration.ofSeconds(1));

        RateLimitDecision afterRefill = limiter.tryAcquire("k");
        assertTrue(afterRefill.allowed());
        assertEquals(1L, afterRefill.remainingTokens());
    }

    @Test
    void keepsIndependentBucketsPerKey() {
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(
                        RateLimitConfig.tokensPerSecond(1, 1),
                        Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

        assertTrue(limiter.tryAcquire("a").allowed());
        assertFalse(limiter.tryAcquire("a").allowed());
        assertTrue(limiter.tryAcquire("b").allowed());
    }

    @Test
    void doesNotConsumeTokensWhenDenied() {
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(
                        RateLimitConfig.tokensPerSecond(5, 1),
                        Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

        assertTrue(limiter.tryAcquire(new RateLimitRequest("k", 3)).allowed());
        RateLimitDecision denied = limiter.tryAcquire(new RateLimitRequest("k", 3));
        assertFalse(denied.allowed());
        assertEquals(2L, denied.remainingTokens());

        RateLimitDecision allowed = limiter.tryAcquire(new RateLimitRequest("k", 2));
        assertTrue(allowed.allowed());
        assertEquals(0L, allowed.remainingTokens());
    }

    @Test
    void deniesWhenPermitsExceedCapacity() {
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(
                        RateLimitConfig.tokensPerSecond(2, 2),
                        Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

        RateLimitDecision denied = limiter.tryAcquire(new RateLimitRequest("k", 3));
        assertFalse(denied.allowed());
        assertEquals(2L, denied.remainingTokens());
        assertEquals(Duration.ZERO, denied.retryAfter());
        assertTrue(limiter.tryAcquire("k").allowed());
    }

    @Test
    void exposesConfigAndRejectsNulls() {
        RateLimitConfig config = RateLimitConfig.tokensPerSecond(5, 1);
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(config);

        assertSame(config, limiter.config());
        assertThrows(NullPointerException.class, () -> new InMemoryRateLimiter(null));
        assertThrows(NullPointerException.class, () -> limiter.tryAcquire((RateLimitRequest) null));
    }
}
