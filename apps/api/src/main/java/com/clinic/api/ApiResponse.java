package com.clinic.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Standard API response wrapper for all endpoints.
 * Provides consistent structure for success and error responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonProperty("success")
    private final boolean success;
    
    @JsonProperty("data")
    private final T data;
    
    @JsonProperty("message")
    private final String message;
    
    @JsonProperty("error_code")
    private final String errorCode;
    
    @JsonProperty("errors")
    private final List<ApiError> errors;
    
    @JsonProperty("timestamp")
    private final String timestamp;
    
    @JsonProperty("path")
    private final String path;

    private ApiResponse(boolean success, T data, String message, String errorCode, 
                       List<ApiError> errors, String path) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.errors = errors;
        this.timestamp = TimestampFormatter.getCurrentIso8601();
        this.path = path;
    }

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null, null, null);
    }

    /**
     * Create a successful response with data and message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null, null);
    }

    /**
     * Create an error response with code and message.
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode, null, null);
    }

    /**
     * Create an error response with code, message, and detailed errors.
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, List<ApiError> errors) {
        return new ApiResponse<>(false, null, message, errorCode, errors, null);
    }

    /**
     * Create an error response with code, message, errors, and request path.
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, List<ApiError> errors, String path) {
        return new ApiResponse<>(false, null, message, errorCode, errors, path);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<ApiError> getErrors() {
        return errors;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errors=" + errors +
                ", timestamp=" + timestamp +
                ", path='" + path + '\'' +
                '}';
    }
}