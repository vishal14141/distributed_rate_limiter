package com.distributedratelimiter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimitConfigTest {

    @Test
    void ofStoresCapacityAndRefill() {
        RateLimitConfig config = RateLimitConfig.of(100, 10, Duration.ofSeconds(1));
        assertEquals(100, config.capacity());
        assertEquals(10, config.refillTokens());
        assertEquals(Duration.ofSeconds(1), config.refillPeriod());
        assertEquals(10.0, config.tokensPerSecond(), 1e-9);
    }

    @Test
    void perSecondUsesOneSecondPeriod() {
        RateLimitConfig config = RateLimitConfig.perSecond(50, 25);
        assertEquals(50, config.capacity());
        assertEquals(25, config.refillTokens());
        assertEquals(Duration.ofSeconds(1), config.refillPeriod());
        assertEquals(25.0, config.tokensPerSecond(), 1e-9);
    }

    @Test
    void tokensPerSecondAccountsForSubSecondPeriod() {
        RateLimitConfig config = RateLimitConfig.of(20, 5, Duration.ofMillis(250));
        assertEquals(20.0, config.tokensPerSecond(), 1e-9);
    }

    @Test
    void rejectsInvalidCapacity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RateLimitConfig.of(0, 1, Duration.ofSeconds(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> RateLimitConfig.of(-5, 1, Duration.ofSeconds(1)));
    }

    @Test
    void rejectsInvalidRefillTokens() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RateLimitConfig.of(1, 0, Duration.ofSeconds(1)));
    }

    @Test
    void rejectsInvalidRefillPeriod() {
        assertThrows(NullPointerException.class, () -> RateLimitConfig.of(1, 1, null));
        assertThrows(IllegalArgumentException.class, () -> RateLimitConfig.of(1, 1, Duration.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () -> RateLimitConfig.of(1, 1, Duration.ofMillis(-1)));
    }

    @Test
    void equalityUsesAllFields() {
        RateLimitConfig a = RateLimitConfig.of(10, 2, Duration.ofSeconds(1));
        RateLimitConfig b = RateLimitConfig.of(10, 2, Duration.ofSeconds(1));
        RateLimitConfig c = RateLimitConfig.of(11, 2, Duration.ofSeconds(1));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
