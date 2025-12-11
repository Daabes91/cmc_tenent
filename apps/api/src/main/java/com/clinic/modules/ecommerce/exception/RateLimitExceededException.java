package com.clinic.modules.ecommerce.exception;

import java.time.Instant;

/**
 * Exception thrown when rate limits are exceeded.
 * Provides information about the rate limit and retry timing.
 */
public class RateLimitExceededException extends EcommerceException {

    private final String operation;
    private final int limit;
    private final int windowSeconds;
    private final Instant retryAfter;

    public RateLimitExceededException(String operation, int limit, int windowSeconds, Instant retryAfter) {
        super(String.format("Rate limit exceeded for %s. Limit: %d requests per %d seconds. Retry after: %s", 
                operation, limit, windowSeconds, retryAfter));
        this.operation = operation;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
        this.retryAfter = retryAfter;
    }

    public RateLimitExceededException(String operation, int limit, int windowSeconds, Instant retryAfter, String message) {
        super(message);
        this.operation = operation;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
        this.retryAfter = retryAfter;
    }

    public String getOperation() {
        return operation;
    }

    public int getLimit() {
        return limit;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public Instant getRetryAfter() {
        return retryAfter;
    }

    /**
     * Get retry after time in seconds from now.
     */
    public long getRetryAfterSeconds() {
        return Math.max(0, retryAfter.getEpochSecond() - Instant.now().getEpochSecond());
    }
}