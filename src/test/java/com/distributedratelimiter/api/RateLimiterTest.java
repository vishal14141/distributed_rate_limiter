package com.distributedratelimiter.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    @Test
    void defaultTryAcquireConsumesOneToken() {
        RecordingLimiter limiter = new RecordingLimiter(RateLimitDecision.allow(4));
        RateLimitDecision decision = limiter.tryAcquire();
        assertTrue(decision.allowed());
        assertEquals(1, limiter.lastRequestedTokens);
        assertEquals(4, decision.remainingTokens());
    }

    @Test
    void implementationsMayRejectNonPositiveTokenCounts() {
        RateLimiter limiter = new RecordingLimiter(RateLimitDecision.allow(0));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(0));
        assertThrows(IllegalArgumentException.class, () -> limiter.tryAcquire(-2));
    }

    private static final class RecordingLimiter implements RateLimiter {
        private final RateLimitDecision decision;
        private int lastRequestedTokens;

        private RecordingLimiter(RateLimitDecision decision) {
            this.decision = decision;
        }

        @Override
        public RateLimitDecision tryAcquire(int tokens) {
            if (tokens < 1) {
                throw new IllegalArgumentException("tokens must be at least 1");
            }
            lastRequestedTokens = tokens;
            return decision;
        }
    }

    @Test
    void deniedDecisionIsReturnedUnchanged() {
        RateLimitDecision denied = RateLimitDecision.deny(0, Duration.ofSeconds(2));
        RateLimiter limiter = new RecordingLimiter(denied);
        assertEquals(denied, limiter.tryAcquire(3));
        assertEquals(3, ((RecordingLimiter) limiter).lastRequestedTokens);
    }
}
