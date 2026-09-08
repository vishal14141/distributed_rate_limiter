package com.distributedratelimiter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.distributedratelimiter.api.RateLimitDecision;
import com.distributedratelimiter.api.RateLimitOutcome;
import com.distributedratelimiter.api.RateLimitRequest;
import com.distributedratelimiter.api.RateLimiter;
import com.distributedratelimiter.config.RateLimitConfig;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Day 4 coverage of token-bucket math: continuous refill, capacity ceiling, remaining-token
 * flooring, and retry-after. Concurrency is covered on Day 5.
 */
class TokenBucketBehaviorTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void defaultKeyUsesSharedBucket() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(1, 1), clock);

        RateLimitDecision first = limiter.tryAcquire();
        assertTrue(first.allowed());
        assertEquals(RateLimiter.DEFAULT_KEY, first.key());
        assertEquals(0L, first.remainingTokens());
        assertEquals(Duration.ZERO, first.retryAfter());

        RateLimitDecision second = limiter.tryAcquire(RateLimiter.DEFAULT_KEY);
        assertFalse(second.allowed());
        assertEquals(RateLimiter.DEFAULT_KEY, second.key());
    }

    @Test
    void allowedDecisionsReportZeroRetryAfterAndAllowedOutcome() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(2, 1), clock);

        RateLimitDecision decision = limiter.tryAcquire("k", 1);
        assertEquals(RateLimitOutcome.ALLOWED, decision.outcome());
        assertTrue(decision.allowed());
        assertEquals(Duration.ZERO, decision.retryAfter());
        assertEquals(1L, decision.remainingTokens());
    }

    @Test
    void partialPeriodAddsFractionalTokensBeforeWholeTokenIsAvailable() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(new RateLimitConfig(2, 2, Duration.ofSeconds(1)), clock);

        assertTrue(limiter.tryAcquire("k").allowed());
        assertTrue(limiter.tryAcquire("k").allowed());

        clock.advance(Duration.ofMillis(250));

        RateLimitDecision stillDenied = limiter.tryAcquire("k");
        assertFalse(stillDenied.allowed());
        assertEquals(0L, stillDenied.remainingTokens());
        assertEquals(Duration.ofMillis(250), stillDenied.retryAfter());
    }

    @Test
    void refillIsCappedAtCapacityAfterLongIdle() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(2, 10), clock);

        assertTrue(limiter.tryAcquire("k").allowed());
        clock.advance(Duration.ofHours(1));

        RateLimitDecision afterIdle = limiter.tryAcquire("k");
        assertTrue(afterIdle.allowed());
        assertEquals(1L, afterIdle.remainingTokens());
        assertFalse(limiter.tryAcquire(new RateLimitRequest("k", 2)).allowed());
    }

    @Test
    void remainingTokensFloorsFractionalBalance() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(new RateLimitConfig(5, 1, Duration.ofSeconds(1)), clock);

        assertTrue(limiter.tryAcquire(new RateLimitRequest("k", 5)).allowed());
        clock.advance(Duration.ofMillis(1500));

        RateLimitDecision deniedForTwo = limiter.tryAcquire(new RateLimitRequest("k", 2));
        assertFalse(deniedForTwo.allowed());
        assertEquals(1L, deniedForTwo.remainingTokens());

        RateLimitDecision allowedOne = limiter.tryAcquire("k");
        assertTrue(allowedOne.allowed());
        assertEquals(0L, allowedOne.remainingTokens());
    }

    @Test
    void retryAfterMatchesMissingTokensAtConfiguredRate() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(4, 2), clock);

        assertTrue(limiter.tryAcquire(new RateLimitRequest("k", 4)).allowed());

        RateLimitDecision needOne = limiter.tryAcquire("k");
        assertFalse(needOne.allowed());
        assertEquals(Duration.ofMillis(500), needOne.retryAfter());

        RateLimitDecision needTwo = limiter.tryAcquire("k", 2);
        assertFalse(needTwo.allowed());
        assertEquals(Duration.ofSeconds(1), needTwo.retryAfter());
    }

    @Test
    void retryAfterShrinksAsTimeAdvancesTowardTheNextToken() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(1, 1), clock);

        assertTrue(limiter.tryAcquire("k").allowed());
        assertEquals(Duration.ofSeconds(1), limiter.tryAcquire("k").retryAfter());

        clock.advance(Duration.ofMillis(400));
        assertEquals(Duration.ofMillis(600), limiter.tryAcquire("k").retryAfter());

        clock.advance(Duration.ofMillis(600));
        RateLimitDecision allowed = limiter.tryAcquire("k");
        assertTrue(allowed.allowed());
        assertEquals(0L, allowed.remainingTokens());
    }

    @Test
    void nonSecondRefillPeriodScalesWaitAndRefill() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(new RateLimitConfig(1, 1, Duration.ofMillis(100)), clock);

        assertTrue(limiter.tryAcquire("k").allowed());
        RateLimitDecision denied = limiter.tryAcquire("k");
        assertFalse(denied.allowed());
        assertEquals(Duration.ofMillis(100), denied.retryAfter());

        clock.advance(Duration.ofMillis(100));
        assertTrue(limiter.tryAcquire("k").allowed());
    }

    @Test
    void zeroElapsedTimeDoesNotAddTokens() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(1, 100), clock);

        assertTrue(limiter.tryAcquire("k").allowed());
        assertFalse(limiter.tryAcquire("k").allowed());
        assertFalse(limiter.tryAcquire("k").allowed());
    }

    @Test
    void clockRewindDoesNotRefillOrConsumeOnDeny() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(2, 1), clock);

        assertTrue(limiter.tryAcquire("k").allowed());
        clock.set(T0.minusSeconds(30));

        RateLimitDecision denied = limiter.tryAcquire(new RateLimitRequest("k", 2));
        assertFalse(denied.allowed());
        assertEquals(1L, denied.remainingTokens());

        clock.set(T0);
        assertTrue(limiter.tryAcquire("k").allowed());
        assertEquals(0L, limiter.tryAcquire("k").remainingTokens());
    }

    @Test
    void deniedRequestsLeaveTokensForLaterSmallerAcquire() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(5, 1), clock);

        assertTrue(limiter.tryAcquire(new RateLimitRequest("k", 4)).allowed());
        assertFalse(limiter.tryAcquire(new RateLimitRequest("k", 2)).allowed());
        assertFalse(limiter.tryAcquire(new RateLimitRequest("k", 2)).allowed());

        RateLimitDecision lastToken = limiter.tryAcquire("k");
        assertTrue(lastToken.allowed());
        assertEquals(0L, lastToken.remainingTokens());
    }

    @Test
    void keysDoNotShareRefillOrRemainingTokens() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(RateLimitConfig.tokensPerSecond(1, 1), clock);

        assertTrue(limiter.tryAcquire("a").allowed());
        clock.advance(Duration.ofMillis(500));
        assertFalse(limiter.tryAcquire("a").allowed());
        assertEquals(Duration.ofMillis(500), limiter.tryAcquire("a").retryAfter());

        RateLimitDecision b = limiter.tryAcquire("b");
        assertTrue(b.allowed());
        assertEquals(0L, b.remainingTokens());
    }

    @Test
    void burstUsesFullCapacityBeforeAnyRefill() {
        MutableClock clock = new MutableClock(T0);
        InMemoryRateLimiter limiter =
                new InMemoryRateLimiter(new RateLimitConfig(10, 1, Duration.ofSeconds(10)), clock);

        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire("burst").allowed(), "permit " + i);
        }
        RateLimitDecision denied = limiter.tryAcquire("burst");
        assertFalse(denied.allowed());
        assertEquals(Duration.ofSeconds(10), denied.retryAfter());
    }
}
