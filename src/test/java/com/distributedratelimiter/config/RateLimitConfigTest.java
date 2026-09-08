package com.distributedratelimiter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimitConfigTest {

    @Test
    void acceptsPositiveCapacityAndRefill() {
        RateLimitConfig config = new RateLimitConfig(100, 10, Duration.ofSeconds(1));

        assertEquals(100, config.capacity());
        assertEquals(100, config.burstCapacity());
        assertEquals(10, config.refillTokens());
        assertEquals(Duration.ofSeconds(1), config.refillPeriod());
    }

    @Test
    void tokensPerSecondSetsOneSecondPeriod() {
        RateLimitConfig config = RateLimitConfig.tokensPerSecond(50, 25);

        assertEquals(50, config.capacity());
        assertEquals(25, config.refillTokens());
        assertEquals(Duration.ofSeconds(1), config.refillPeriod());
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimitConfig(0, 1, Duration.ofSeconds(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimitConfig(-1, 1, Duration.ofSeconds(1)));
    }

    @Test
    void rejectsNonPositiveRefillTokens() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimitConfig(1, 0, Duration.ofSeconds(1)));
    }

    @Test
    void rejectsNullOrNonPositivePeriod() {
        assertThrows(NullPointerException.class, () -> new RateLimitConfig(1, 1, null));
        assertThrows(
                IllegalArgumentException.class, () -> new RateLimitConfig(1, 1, Duration.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimitConfig(1, 1, Duration.ofMillis(-1)));
    }
}
