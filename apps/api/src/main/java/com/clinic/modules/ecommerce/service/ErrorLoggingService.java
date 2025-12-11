package com.clinic.modules.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for logging and monitoring e-commerce errors.
 * Provides structured logging, error tracking, and alerting capabilities.
 */
@Service
public class ErrorLoggingService {

    private static final Logger log = LoggerFactory.getLogger(ErrorLoggingService.class);
    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY");
    private static final Logger businessLog = LoggerFactory.getLogger("BUSINESS");

    // Error tracking for monitoring
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastErrorTimes = new ConcurrentHashMap<>();

    /**
     * Log a validation error with context.
     */
    public void logValidationError(String operation, String tenantId, String userId, 
                                 String errorDetails, Map<String, Object> context) {
        try {
            MDC.put("operation", operation);
            MDC.put("tenantId", tenantId);
            MDC.put("userId", userId);
            MDC.put("errorType", "VALIDATION");
            
            if (context != null) {
                context.forEach((key, value) -> MDC.put("context." + key, String.valueOf(value)));
            }
            
            log.warn("Validation error in {}: {}", operation, errorDetails);
            incrementErrorCount("validation." + operation);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log a business logic error with context.
     */
    public void logBusinessError(String operation, String tenantId, String userId, 
                               String errorDetails, Map<String, Object> context) {
        try {
            MDC.put("operation", operation);
            MDC.put("tenantId", tenantId);
            MDC.put("userId", userId);
            MDC.put("errorType", "BUSINESS");
            
            if (context != null) {
                context.forEach((key, value) -> MDC.put("context." + key, String.valueOf(value)));
            }
            
            businessLog.warn("Business error in {}: {}", operation, errorDetails);
            incrementErrorCount("business." + operation);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log a security-related error with high priority.
     */
    public void logSecurityError(String operation, String tenantId, String userId, 
                               String errorDetails, Map<String, Object> context) {
        try {
            MDC.put("operation", operation);
            MDC.put("tenantId", tenantId);
            MDC.put("userId", userId);
            MDC.put("errorType", "SECURITY");
            MDC.put("severity", "HIGH");
            
            if (context != null) {
                context.forEach((key, value) -> MDC.put("context." + key, String.valueOf(value)));
            }
            
            securityLog.error("SECURITY ALERT - {}: {}", operation, errorDetails);
            incrementErrorCount("security." + operation);
            
            // Check if this is a repeated security error that needs escalation
            checkSecurityErrorFrequency(operation);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log a system error with full context.
     */
    public void logSystemError(String operation, String tenantId, String userId, 
                             String errorDetails, Throwable throwable, Map<String, Object> context) {
        try {
            MDC.put("operation", operation);
            MDC.put("tenantId", tenantId);
            MDC.put("userId", userId);
            MDC.put("errorType", "SYSTEM");
            
            if (context != null) {
                context.forEach((key, value) -> MDC.put("context." + key, String.valueOf(value)));
            }
            
            if (throwable != null) {
                log.error("System error in {}: {}", operation, errorDetails, throwable);
            } else {
                log.error("System error in {}: {}", operation, errorDetails);
            }
            
            incrementErrorCount("system." + operation);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log a payment-related error with special handling.
     */
    public void logPaymentError(String operation, String tenantId, String orderId, 
                              String paymentProvider, String errorCode, String errorDetails, 
                              Map<String, Object> context) {
        try {
            MDC.put("operation", operation);
            MDC.put("tenantId", tenantId);
            MDC.put("orderId", orderId);
            MDC.put("paymentProvider", paymentProvider);
            MDC.put("errorCode", errorCode);
            MDC.put("errorType", "PAYMENT");
            MDC.put("severity", "HIGH");
            
            if (context != null) {
                context.forEach((key, value) -> MDC.put("context." + key, String.valueOf(value)));
            }
            
            businessLog.error("Payment error in {}: provider={} code={} details={}", 
                    operation, paymentProvider, errorCode, errorDetails);
            incrementErrorCount("payment." + operation);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log a rate limiting event.
     */
    public void logRateLimitEvent(String operation, String clientId, String tenantId, 
                                int limit, int windowSeconds) {
        try {
            MDC.put("operation", operation);
            MDC.put("clientId", clientId);
            MDC.put("tenantId", tenantId);
            MDC.put("limit", String.valueOf(limit));
            MDC.put("windowSeconds", String.valueOf(windowSeconds));
            MDC.put("errorType", "RATE_LIMIT");
            
            log.warn("Rate limit exceeded for {}: client={} limit={} window={}s", 
                    operation, clientId, limit, windowSeconds);
            incrementErrorCount("rateLimit." + operation);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log a feature access denial.
     */
    public void logFeatureAccessDenied(String feature, String tenantId, String userId, String reason) {
        try {
            MDC.put("feature", feature);
            MDC.put("tenantId", tenantId);
            MDC.put("userId", userId);
            MDC.put("reason", reason);
            MDC.put("errorType", "FEATURE_ACCESS");
            
            log.warn("Feature access denied for {}: tenant={} user={} reason={}", 
                    feature, tenantId, userId, reason);
            incrementErrorCount("featureAccess." + feature);
            
        } finally {
            MDC.clear();
        }
    }

    /**
     * Get error statistics for monitoring.
     */
    public Map<String, Long> getErrorStatistics() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        errorCounts.forEach((key, count) -> stats.put(key, count.get()));
        return stats;
    }

    /**
     * Reset error statistics (useful for testing or periodic resets).
     */
    public void resetErrorStatistics() {
        errorCounts.clear();
        lastErrorTimes.clear();
        log.info("Error statistics reset");
    }

    /**
     * Check if error rate is concerning and needs attention.
     */
    public boolean isErrorRateConcerning(String errorType, int thresholdPerMinute) {
        AtomicLong count = errorCounts.get(errorType);
        if (count == null) {
            return false;
        }
        
        Instant lastError = lastErrorTimes.get(errorType);
        if (lastError == null) {
            return false;
        }
        
        // Check if we've had too many errors in the last minute
        Instant oneMinuteAgo = Instant.now().minusSeconds(60);
        if (lastError.isAfter(oneMinuteAgo) && count.get() > thresholdPerMinute) {
            log.warn("High error rate detected for {}: {} errors in last minute (threshold: {})", 
                    errorType, count.get(), thresholdPerMinute);
            return true;
        }
        
        return false;
    }

    private void incrementErrorCount(String errorType) {
        errorCounts.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
        lastErrorTimes.put(errorType, Instant.now());
    }

    private void checkSecurityErrorFrequency(String operation) {
        String securityKey = "security." + operation;
        AtomicLong count = errorCounts.get(securityKey);
        
        if (count != null && count.get() > 5) {
            Instant lastError = lastErrorTimes.get(securityKey);
            if (lastError != null && lastError.isAfter(Instant.now().minusSeconds(300))) {
                // More than 5 security errors in 5 minutes - escalate
                securityLog.error("CRITICAL SECURITY ALERT - High frequency security errors in {}: {} errors in 5 minutes", 
                        operation, count.get());
            }
        }
    }
}