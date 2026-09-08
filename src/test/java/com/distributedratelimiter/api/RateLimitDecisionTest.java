package com.distributedratelimiter.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RateLimitDecisionTest {

    @Test
    void allowStoresRemainingTokensAndEmptyRetryAfter() {
        RateLimitDecision decision = RateLimitDecision.allow(7);
        assertTrue(decision.allowed());
        assertFalse(decision.denied());
        assertEquals(7, decision.remainingTokens());
        assertEquals(Optional.empty(), decision.retryAfter());
    }

    @Test
    void denyStoresRemainingTokensAndRetryAfter() {
        RateLimitDecision decision = RateLimitDecision.deny(0, Duration.ofMillis(250));
        assertTrue(decision.denied());
        assertFalse(decision.allowed());
        assertEquals(0, decision.remainingTokens());
        assertEquals(Optional.of(Duration.ofMillis(250)), decision.retryAfter());
    }

    @Test
    void denyMayUseZeroRetryAfter() {
        RateLimitDecision decision = RateLimitDecision.deny(3, Duration.ZERO);
        assertTrue(decision.denied());
        assertEquals(Optional.of(Duration.ZERO), decision.retryAfter());
    }

    @Test
    void rejectsNegativeRemainingTokens() {
        assertThrows(IllegalArgumentException.class, () -> RateLimitDecision.allow(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> RateLimitDecision.deny(-1, Duration.ofSeconds(1)));
    }

    @Test
    void denyRejectsNullOrNegativeRetryAfter() {
        assertThrows(NullPointerException.class, () -> RateLimitDecision.deny(0, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> RateLimitDecision.deny(0, Duration.ofSeconds(-1)));
    }

    @Test
    void equalDecisionsCompareEqual() {
        RateLimitDecision a = RateLimitDecision.deny(0, Duration.ofSeconds(1));
        RateLimitDecision b = RateLimitDecision.deny(0, Duration.ofSeconds(1));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
