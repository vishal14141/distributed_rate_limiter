package com.distributedratelimiter.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.distributedratelimiter.config.RateLimitConfig;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimiterDefaultsTest {

    @Test
    void defaultOverloadsDelegateToRequest() {
        RecordingRateLimiter limiter =
                new RecordingRateLimiter(RateLimitConfig.tokensPerSecond(10, 10));

        RateLimitDecision fromKey = limiter.tryAcquire("user-1");
        assertEquals("user-1", limiter.lastRequest.key());
        assertEquals(1L, limiter.lastRequest.permits());
        assertEquals(RateLimitOutcome.ALLOWED, fromKey.outcome());

        limiter.tryAcquire("user-2", 3);
        assertEquals("user-2", limiter.lastRequest.key());
        assertEquals(3L, limiter.lastRequest.permits());

        limiter.tryAcquire();
        assertEquals(RateLimiter.DEFAULT_KEY, limiter.lastRequest.key());
        assertEquals(1L, limiter.lastRequest.permits());
    }

    @Test
    void exposesConfiguredLimit() {
        RateLimitConfig config = new RateLimitConfig(20, 5, Duration.ofMillis(100));
        RateLimiter limiter = new RecordingRateLimiter(config);

        assertSame(config, limiter.config());
    }

    private static final class RecordingRateLimiter implements RateLimiter {

        private final RateLimitConfig config;
        private RateLimitRequest lastRequest;

        private RecordingRateLimiter(RateLimitConfig config) {
            this.config = config;
        }

        @Override
        public RateLimitConfig config() {
            return config;
        }

        @Override
        public RateLimitDecision tryAcquire(RateLimitRequest request) {
            this.lastRequest = request;
            return RateLimitDecision.allow(request.key(), config.capacity());
        }
    }
}
