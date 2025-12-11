package com.clinic.modules.ecommerce.dto.validation;

import com.clinic.api.ApiError;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Structured validation error response for e-commerce operations.
 * Provides detailed field-level validation errors with context.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {

    private final String message;
    private final String errorCode;
    private final List<ApiError> fieldErrors;
    private final String requestPath;
    private final Instant timestamp;

    public ValidationErrorResponse(String message, String errorCode, List<ApiError> fieldErrors, String requestPath) {
        this.message = message;
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
        this.requestPath = requestPath;
        this.timestamp = Instant.now();
    }

    public static ValidationErrorResponse of(String message, List<ApiError> fieldErrors) {
        return new ValidationErrorResponse(message, "VALIDATION_ERROR", fieldErrors, null);
    }

    public static ValidationErrorResponse of(String message, String errorCode, List<ApiError> fieldErrors) {
        return new ValidationErrorResponse(message, errorCode, fieldErrors, null);
    }

    public static ValidationErrorResponse of(String message, String errorCode, List<ApiError> fieldErrors, String requestPath) {
        return new ValidationErrorResponse(message, errorCode, fieldErrors, requestPath);
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<ApiError> getFieldErrors() {
        return fieldErrors;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}