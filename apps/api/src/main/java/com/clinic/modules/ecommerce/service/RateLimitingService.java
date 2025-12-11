package com.clinic.modules.ecommerce.service;

import com.clinic.modules.ecommerce.config.RateLimitingConfig;
import com.clinic.modules.ecommerce.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing rate limiting across e-commerce operations.
 * Uses in-memory storage for simplicity - in production, consider Redis.
 */
@Service
public class RateLimitingService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingService.class);

    private final RateLimitingConfig config;
    private final ConcurrentHashMap<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingService(RateLimitingConfig config) {
        this.config = config;
    }

    /**
     * Check if the operation is allowed for the given key.
     * 
     * @param operation the operation type
     * @param key the rate limiting key (usually IP address or user ID)
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkRateLimit(String operation, String key) {
        if (!config.isEnabled()) {
            return;
        }

        int limit = getLimitForOperation(operation);
        if (limit <= 0) {
            return; // No limit configured
        }

        String bucketKey = operation + ":" + key;
        RateLimitBucket bucket = buckets.computeIfAbsent(bucketKey, 
                k -> new RateLimitBucket(limit, config.getWindowSizeSeconds()));

        if (!bucket.tryConsume()) {
            Instant retryAfter = bucket.getResetTime();
            log.warn("Rate limit exceeded for operation {} and key {}: limit={}, window={}s", 
                    operation, key, limit, config.getWindowSizeSeconds());
            throw new RateLimitExceededException(operation, limit, config.getWindowSizeSeconds(), retryAfter);
        }

        log.debug("Rate limit check passed for operation {} and key {}: remaining={}", 
                operation, key, bucket.getRemainingTokens());
    }

    /**
     * Get the current rate limit status for an operation and key.
     */
    public RateLimitStatus getRateLimitStatus(String operation, String key) {
        if (!config.isEnabled()) {
            return new RateLimitStatus(Integer.MAX_VALUE, Integer.MAX_VALUE, Instant.now().plusSeconds(3600));
        }

        int limit = getLimitForOperation(operation);
        if (limit <= 0) {
            return new RateLimitStatus(Integer.MAX_VALUE, Integer.MAX_VALUE, Instant.now().plusSeconds(3600));
        }

        String bucketKey = operation + ":" + key;
        RateLimitBucket bucket = buckets.get(bucketKey);
        
        if (bucket == null) {
            return new RateLimitStatus(limit, limit, Instant.now().plusSeconds(config.getWindowSizeSeconds()));
        }

        return new RateLimitStatus(limit, bucket.getRemainingTokens(), bucket.getResetTime());
    }

    /**
     * Clear rate limit data for a specific key (useful for testing or admin operations).
     */
    public void clearRateLimit(String operation, String key) {
        String bucketKey = operation + ":" + key;
        buckets.remove(bucketKey);
        log.info("Cleared rate limit for operation {} and key {}", operation, key);
    }

    /**
     * Clean up expired buckets to prevent memory leaks.
     */
    public void cleanupExpiredBuckets() {
        Instant now = Instant.now();
        buckets.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private int getLimitForOperation(String operation) {
        return switch (operation.toLowerCase()) {
            case "cart" -> config.getCartOperationsPerMinute();
            case "order" -> config.getOrderCreationPerMinute();
            case "payment" -> config.getPaymentOperationsPerMinute();
            case "product" -> config.getProductBrowsingPerMinute();
            case "search" -> config.getSearchOperationsPerMinute();
            case "admin" -> config.getAdminOperationsPerMinute();
            default -> 0; // No limit for unknown operations
        };
    }

    /**
     * Rate limit bucket implementation using token bucket algorithm.
     */
    private static class RateLimitBucket {
        private final int capacity;
        private final int windowSeconds;
        private final AtomicInteger tokens;
        private volatile Instant lastRefill;
        private volatile Instant resetTime;

        public RateLimitBucket(int capacity, int windowSeconds) {
            this.capacity = capacity;
            this.windowSeconds = windowSeconds;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefill = Instant.now();
            this.resetTime = lastRefill.plusSeconds(windowSeconds);
        }

        public synchronized boolean tryConsume() {
            refillIfNeeded();
            
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            
            return false;
        }

        public int getRemainingTokens() {
            refillIfNeeded();
            return tokens.get();
        }

        public Instant getResetTime() {
            return resetTime;
        }

        public boolean isExpired(Instant now) {
            return now.isAfter(resetTime.plusSeconds(windowSeconds));
        }

        private synchronized void refillIfNeeded() {
            Instant now = Instant.now();
            
            if (now.isAfter(resetTime)) {
                // Reset the bucket
                tokens.set(capacity);
                lastRefill = now;
                resetTime = now.plusSeconds(windowSeconds);
            }
        }
    }

    /**
     * Rate limit status information.
     */
    public static class RateLimitStatus {
        private final int limit;
        private final int remaining;
        private final Instant resetTime;

        public RateLimitStatus(int limit, int remaining, Instant resetTime) {
            this.limit = limit;
            this.remaining = remaining;
            this.resetTime = resetTime;
        }

        public int getLimit() {
            return limit;
        }

        public int getRemaining() {
            return remaining;
        }

        public Instant getResetTime() {
            return resetTime;
        }

        public long getResetTimeSeconds() {
            return resetTime.getEpochSecond();
        }
    }
}