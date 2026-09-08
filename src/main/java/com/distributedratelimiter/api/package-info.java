/**
 * Public rate-limiter types: requests, allow/deny decisions, and the {@code RateLimiter} port.
 *
 * <p>Callers should depend on this package (and {@code config}) rather than storage or HTTP
 * adapters. The in-memory implementation is {@code
 * com.distributedratelimiter.core.InMemoryRateLimiter}.
 */
package com.distributedratelimiter.api;
