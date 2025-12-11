package com.clinic.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Represents a single error in an API response.
 * Used for field-level validation errors and detailed error information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @JsonProperty("field")
    private final String field;
    
    @JsonProperty("message")
    private final String message;
    
    @JsonProperty("rejected_value")
    private final Object rejectedValue;
    
    @JsonProperty("code")
    private final String code;
    
    @JsonProperty("timestamp")
    private final String timestamp;

    private ApiError(String field, String message, Object rejectedValue, String code) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
        this.code = code;
        this.timestamp = TimestampFormatter.getCurrentIso8601();
    }

    /**
     * Create a field-level error.
     */
    public static ApiError of(String field, String message) {
        return new ApiError(field, message, null, null);
    }

    /**
     * Create a field-level error with rejected value.
     */
    public static ApiError of(String field, String message, Object rejectedValue) {
        return new ApiError(field, message, rejectedValue, null);
    }

    /**
     * Create a field-level error with code and rejected value.
     */
    public static ApiError of(String field, String message, Object rejectedValue, String code) {
        return new ApiError(field, message, rejectedValue, code);
    }

    /**
     * Create a general error without field association.
     */
    public static ApiError general(String message) {
        return new ApiError(null, message, null, null);
    }

    /**
     * Create a general error with code.
     */
    public static ApiError general(String message, String code) {
        return new ApiError(null, message, null, code);
    }

    // Getters
    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public String getCode() {
        return code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "field='" + field + '\'' +
                ", message='" + message + '\'' +
                ", rejectedValue=" + rejectedValue +
                ", code='" + code + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}