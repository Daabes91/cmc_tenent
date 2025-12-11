package com.clinic.api;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating standardized API responses.
 * Provides convenience methods for common response patterns.
 */
public class ApiResponseFactory {

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * Create a successful response with data and message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.success(data, message);
    }



    /**
     * Create a successful response with code, message, and data.
     */
    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return ApiResponse.success(data, message);
    }

    /**
     * Create a successful response with code and message (no data).
     */
    public static ApiResponse<Void> success(String code, String message) {
        return ApiResponse.success(null, message);
    }

    /**
     * Create a successful response with code, message, data, and additional parameters.
     * This method provides compatibility with existing codebase patterns.
     */
    public static <T> ApiResponse<T> success(String code, String message, T data, Map<String, Object> metadata, Map<String, Object> links) {
        return ApiResponse.success(data, message);
    }

    /**
     * Create an error response with code and message.
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.error(errorCode, message, null, getCurrentRequestPath());
    }

    /**
     * Create an error response with code, message, and detailed errors.
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, List<ApiError> errors) {
        return ApiResponse.error(errorCode, message, errors, getCurrentRequestPath());
    }

    /**
     * Create an error response with code, message, errors, and metadata.
     * This method provides compatibility with existing codebase patterns.
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, List<ApiError> errors, Map<String, Object> metadata) {
        return ApiResponse.error(errorCode, message, errors, getCurrentRequestPath());
    }

    /**
     * Create an error response with type, code, message, and errors.
     * This method provides compatibility with existing codebase patterns.
     */
    public static <T> ApiResponse<T> errorWithType(String errorCode, String message, List<ApiError> errors) {
        return ApiResponse.error(errorCode, message, errors, getCurrentRequestPath());
    }

    /**
     * Create a validation error response.
     */
    public static <T> ApiResponse<T> validationError(String message, List<ApiError> errors) {
        return ApiResponse.error("VALIDATION_ERROR", message, errors, getCurrentRequestPath());
    }

    /**
     * Create a not found error response.
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error("NOT_FOUND", message, null, getCurrentRequestPath());
    }

    /**
     * Create an unauthorized error response.
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.error("UNAUTHORIZED", message, null, getCurrentRequestPath());
    }

    /**
     * Create a forbidden error response.
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.error("FORBIDDEN", message, null, getCurrentRequestPath());
    }

    /**
     * Create a conflict error response.
     */
    public static <T> ApiResponse<T> conflict(String message) {
        return ApiResponse.error("CONFLICT", message, null, getCurrentRequestPath());
    }

    /**
     * Create an internal server error response.
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return ApiResponse.error("INTERNAL_SERVER_ERROR", message, null, getCurrentRequestPath());
    }

    /**
     * Create a service unavailable error response.
     */
    public static <T> ApiResponse<T> serviceUnavailable(String message) {
        return ApiResponse.error("SERVICE_UNAVAILABLE", message, null, getCurrentRequestPath());
    }

    /**
     * Create a bad gateway error response.
     */
    public static <T> ApiResponse<T> badGateway(String message) {
        return ApiResponse.error("BAD_GATEWAY", message, null, getCurrentRequestPath());
    }

    /**
     * Create a rate limit exceeded error response.
     */
    public static <T> ApiResponse<T> rateLimitExceeded(String message) {
        return ApiResponse.error("RATE_LIMIT_EXCEEDED", message, null, getCurrentRequestPath());
    }

    /**
     * Create a payment required error response.
     */
    public static <T> ApiResponse<T> paymentRequired(String message) {
        return ApiResponse.error("PAYMENT_REQUIRED", message, null, getCurrentRequestPath());
    }

    /**
     * Create a payment required error response with details.
     */
    public static <T> ApiResponse<T> paymentRequired(String message, List<ApiError> errors) {
        return ApiResponse.error("PAYMENT_REQUIRED", message, errors, getCurrentRequestPath());
    }

    /**
     * Get the current request path for error context.
     */
    private static String getCurrentRequestPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            // Ignore - we'll return null if we can't get the path
        }
        return null;
    }
}