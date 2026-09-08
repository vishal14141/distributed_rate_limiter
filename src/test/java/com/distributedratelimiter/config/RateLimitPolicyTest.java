package com.distributedratelimiter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RateLimitPolicyTest {

    @Test
    void bindsNameToConfig() {
        RateLimitConfig config = RateLimitConfig.tokensPerSecond(10, 5);
        RateLimitPolicy policy = new RateLimitPolicy("api", config);

        assertEquals("api", policy.name());
        assertEquals(config, policy.config());
    }

    @Test
    void rejectsBlankName() {
        RateLimitConfig config = RateLimitConfig.tokensPerSecond(10, 5);
        assertThrows(NullPointerException.class, () -> new RateLimitPolicy(null, config));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitPolicy("  ", config));
    }

    @Test
    void rejectsNullConfig() {
        assertThrows(NullPointerException.class, () -> new RateLimitPolicy("api", null));
    }
}
