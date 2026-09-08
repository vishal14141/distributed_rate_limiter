package com.distributedratelimiter.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimitRequestTest {

    @Test
    void onePermitUsesCountOfOne() {
        RateLimitRequest request = RateLimitRequest.onePermit("client-a");

        assertEquals("client-a", request.key());
        assertEquals(1L, request.permits());
    }

    @Test
    void rejectsBlankKey() {
        assertThrows(NullPointerException.class, () -> new RateLimitRequest(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitRequest(" ", 1));
    }

    @Test
    void rejectsNonPositivePermits() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimitRequest("k", 0));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitRequest("k", -2));
    }
}

class RateLimitDecisionTest {

    @Test
    void allowHasZeroRetryAfter() {
        RateLimitDecision decision = RateLimitDecision.allow("k", 7);

        assertTrue(decision.allowed());
        assertEquals(RateLimitOutcome.ALLOWED, decision.outcome());
        assertEquals("k", decision.key());
        assertEquals(7L, decision.remainingTokens());
        assertEquals(Duration.ZERO, decision.retryAfter());
    }

    @Test
    void denyPreservesRetryAfter() {
        RateLimitDecision decision = RateLimitDecision.deny("k", 0, Duration.ofMillis(250));

        assertFalse(decision.allowed());
        assertEquals(RateLimitOutcome.DENIED, decision.outcome());
        assertEquals(Duration.ofMillis(250), decision.retryAfter());
    }

    @Test
    void rejectsNegativeRemainingOrRetry() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimitDecision(RateLimitOutcome.ALLOWED, "k", -1, Duration.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () -> RateLimitDecision.deny("k", 0, Duration.ofSeconds(-1)));
    }
}
