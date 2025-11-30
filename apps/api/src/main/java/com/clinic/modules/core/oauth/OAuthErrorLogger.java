package com.clinic.modules.core.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized OAuth error logging service.
 * Provides structured logging for OAuth-related errors with appropriate severity levels.
 */
@Component
public class OAuthErrorLogger {

    private static final Logger logger = LoggerFactory.getLogger(OAuthErrorLogger.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    public enum ErrorType {
        USER_CANCELLED,
        INVALID_STATE,
        INVALID_TOKEN,
        GOOGLE_API_ERROR,
        NETWORK_ERROR,
        TENANT_ERROR,
        ACCOUNT_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * Log an OAuth error with context
     */
    public void logError(
            ErrorType errorType,
            String message,
            Throwable throwable,
            Map<String, Object> context
    ) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("errorType", errorType);
        logData.put("message", message);
        logData.put("timestamp", Instant.now().toString());
        
        if (context != null) {
            logData.putAll(context);
        }

        // Log security-related errors to security logger
        if (isSecurityError(errorType)) {
            securityLogger.error("[OAuth Security Error] {}", logData, throwable);
        } else if (errorType == ErrorType.USER_CANCELLED) {
            // User cancellation is informational
            logger.info("[OAuth User Action] {}", logData);
        } else {
            logger.error("[OAuth Error] {}", logData, throwable);
        }
    }

    /**
     * Log an OAuth error without throwable
     */
    public void logError(
            ErrorType errorType,
            String message,
            Map<String, Object> context
    ) {
        logError(errorType, message, null, context);
    }

    /**
     * Log a successful OAuth event
     */
    public void logSuccess(String message, Map<String, Object> context) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("message", message);
        logData.put("timestamp", Instant.now().toString());
        
        if (context != null) {
            logData.putAll(context);
        }

        logger.info("[OAuth Success] {}", logData);
    }

    /**
     * Log a warning for OAuth flow
     */
    public void logWarning(String message, Map<String, Object> context) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("message", message);
        logData.put("timestamp", Instant.now().toString());
        
        if (context != null) {
            logData.putAll(context);
        }

        logger.warn("[OAuth Warning] {}", logData);
    }

    /**
     * Determine if an error type is security-related
     */
    private boolean isSecurityError(ErrorType errorType) {
        return errorType == ErrorType.INVALID_STATE || 
               errorType == ErrorType.INVALID_TOKEN;
    }

    /**
     * Create context map for tenant-related errors
     */
    public static Map<String, Object> tenantContext(String tenantSlug, Long tenantId) {
        Map<String, Object> context = new HashMap<>();
        if (tenantSlug != null) {
            context.put("tenantSlug", tenantSlug);
        }
        if (tenantId != null) {
            context.put("tenantId", tenantId);
        }
        return context;
    }

    /**
     * Create context map for patient-related errors
     */
    public static Map<String, Object> patientContext(Long patientId, String email) {
        Map<String, Object> context = new HashMap<>();
        if (patientId != null) {
            context.put("patientId", patientId);
        }
        if (email != null) {
            context.put("email", email);
        }
        return context;
    }

    /**
     * Create context map for Google API errors
     */
    public static Map<String, Object> googleApiContext(String endpoint, Integer statusCode) {
        Map<String, Object> context = new HashMap<>();
        if (endpoint != null) {
            context.put("googleEndpoint", endpoint);
        }
        if (statusCode != null) {
            context.put("googleStatusCode", statusCode);
        }
        return context;
    }
}
