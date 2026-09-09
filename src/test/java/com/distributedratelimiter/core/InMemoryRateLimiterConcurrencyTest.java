package com.distributedratelimiter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.distributedratelimiter.api.RateLimitDecision;
import com.distributedratelimiter.api.RateLimitRequest;
import com.distributedratelimiter.config.RateLimitConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Day 5: concurrent acquires must not overspend a bucket or leak tokens across keys.
 *
 * <p>A frozen clock is used so refill cannot hide a race by adding tokens during the test.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class InMemoryRateLimiterConcurrencyTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void concurrentAcquiresOnOneKeyNeverExceedCapacity() throws Exception {
        int capacity = 250;
        InMemoryRateLimiter limiter = limiter(RateLimitConfig.tokensPerSecond(capacity, 1));

        int threads = 32;
        int attemptsPerThread = 40;
        LongAdder allowed = new LongAdder();
        LongAdder denied = new LongAdder();

        runConcurrently(
                threads,
                threadIndex -> {
                    for (int i = 0; i < attemptsPerThread; i++) {
                        if (limiter.tryAcquire("hot").allowed()) {
                            allowed.increment();
                        } else {
                            denied.increment();
                        }
                    }
                });

        assertEquals(capacity, allowed.sum());
        assertEquals((long) threads * attemptsPerThread - capacity, denied.sum());

        RateLimitDecision exhausted = limiter.tryAcquire("hot");
        assertFalse(exhausted.allowed());
        assertEquals(0L, exhausted.remainingTokens());
    }

    @Test
    void thunderingHerdOnEmptyBucketDoesNotOverspend() throws Exception {
        int capacity = 64;
        InMemoryRateLimiter limiter = limiter(RateLimitConfig.tokensPerSecond(capacity, 5));

        int threads = capacity * 4;
        CyclicBarrier start = new CyclicBarrier(threads);
        AtomicInteger allowed = new AtomicInteger();

        runConcurrently(
                threads,
                threadIndex -> {
                    start.await(10, TimeUnit.SECONDS);
                    if (limiter.tryAcquire("burst").allowed()) {
                        allowed.incrementAndGet();
                    }
                });

        assertEquals(capacity, allowed.get());
        assertFalse(limiter.tryAcquire("burst").allowed());
    }

    @Test
    void concurrentMultiPermitAcquiresDoNotOversubscribe() throws Exception {
        int capacity = 100;
        long permits = 7;
        InMemoryRateLimiter limiter = limiter(RateLimitConfig.tokensPerSecond(capacity, 1));

        int threads = 40;
        LongAdder allowedCalls = new LongAdder();
        LongAdder consumed = new LongAdder();

        runConcurrently(
                threads,
                threadIndex -> {
                    for (int i = 0; i < 10; i++) {
                        RateLimitDecision decision =
                                limiter.tryAcquire(new RateLimitRequest("k", permits));
                        if (decision.allowed()) {
                            allowedCalls.increment();
                            consumed.add(permits);
                        }
                    }
                });

        long expectedAllowed = capacity / permits;
        assertEquals(expectedAllowed, allowedCalls.sum());
        assertEquals(expectedAllowed * permits, consumed.sum());

        long leftover = capacity - consumed.sum();
        RateLimitDecision remainder = limiter.tryAcquire(new RateLimitRequest("k", leftover));
        assertTrue(remainder.allowed());
        assertEquals(0L, remainder.remainingTokens());
        assertFalse(limiter.tryAcquire("k").allowed());
    }

    @Test
    void concurrentKeysStayIsolated() throws Exception {
        int keys = 80;
        int capacity = 5;
        InMemoryRateLimiter limiter = limiter(RateLimitConfig.tokensPerSecond(capacity, 1));

        int threads = 16;
        int[] allowedPerKey = new int[keys];

        runConcurrently(
                threads,
                threadIndex -> {
                    int[] local = new int[keys];
                    for (int round = 0; round < capacity + 3; round++) {
                        for (int key = 0; key < keys; key++) {
                            if (limiter.tryAcquire("key-" + key).allowed()) {
                                local[key]++;
                            }
                        }
                    }
                    synchronized (allowedPerKey) {
                        for (int key = 0; key < keys; key++) {
                            allowedPerKey[key] += local[key];
                        }
                    }
                });

        for (int key = 0; key < keys; key++) {
            assertEquals(capacity, allowedPerKey[key], "key-" + key);
            RateLimitDecision denied = limiter.tryAcquire("key-" + key);
            assertFalse(denied.allowed());
            assertEquals(0L, denied.remainingTokens());
        }
    }

    @Test
    void concurrentFirstTouchCreatesIndependentBuckets() throws Exception {
        int keys = 2_000;
        InMemoryRateLimiter limiter = limiter(RateLimitConfig.tokensPerSecond(1, 1));

        int threads = 32;
        LongAdder allowed = new LongAdder();

        runConcurrently(
                threads,
                threadIndex -> {
                    for (int key = threadIndex; key < keys; key += threads) {
                        if (limiter.tryAcquire("new-" + key).allowed()) {
                            allowed.increment();
                        }
                    }
                });

        assertEquals(keys, allowed.sum());
        for (int i = 0; i < 50; i++) {
            assertFalse(limiter.tryAcquire("new-" + i).allowed());
            assertTrue(limiter.tryAcquire("other-" + i).allowed());
        }
    }

    @Test
    void deniedConcurrentRequestsDoNotConsumeTokens() throws Exception {
        InMemoryRateLimiter limiter = limiter(RateLimitConfig.tokensPerSecond(10, 1));

        assertTrue(limiter.tryAcquire(new RateLimitRequest("k", 8)).allowed());

        int threads = 24;
        LongAdder allowed = new LongAdder();
        runConcurrently(
                threads,
                threadIndex -> {
                    if (limiter.tryAcquire(new RateLimitRequest("k", 5)).allowed()) {
                        allowed.increment();
                    }
                });

        assertEquals(0, allowed.sum());
        RateLimitDecision leftover = limiter.tryAcquire(new RateLimitRequest("k", 2));
        assertTrue(leftover.allowed());
        assertEquals(0L, leftover.remainingTokens());
    }

    private static InMemoryRateLimiter limiter(RateLimitConfig config) {
        return new InMemoryRateLimiter(config, Clock.fixed(T0, ZoneOffset.UTC));
    }

    private static void runConcurrently(int threads, ConcurrentTask task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier ready = new CyclicBarrier(threads);
        List<Callable<Void>> work = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            int index = i;
            work.add(
                    () -> {
                        ready.await(10, TimeUnit.SECONDS);
                        task.run(index);
                        return null;
                    });
        }
        try {
            List<Future<Void>> futures = pool.invokeAll(work, 20, TimeUnit.SECONDS);
            for (Future<Void> future : futures) {
                future.get(1, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @FunctionalInterface
    private interface ConcurrentTask {
        void run(int threadIndex) throws Exception;
    }
}
