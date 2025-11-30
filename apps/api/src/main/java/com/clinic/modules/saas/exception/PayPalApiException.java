package com.clinic.modules.saas.exception;

/**
 * Exception thrown when a PayPal API call fails.
 * Maps to HTTP 502 Bad Gateway.
 */
public class PayPalApiException extends RuntimeException {
    private final String operation;
    private final Integer statusCode;

    public PayPalApiException(String operation, String message) {
        super("PayPal API error during " + operation + ": " + message);
        this.operation = operation;
        this.statusCode = null;
    }

    public PayPalApiException(String operation, String message, Throwable cause) {
        super("PayPal API error during " + operation + ": " + message, cause);
        this.operation = operation;
        this.statusCode = null;
    }

    public PayPalApiException(String operation, Integer statusCode, String message) {
        super("PayPal API error during " + operation + " (HTTP " + statusCode + "): " + message);
        this.operation = operation;
        this.statusCode = statusCode;
    }

    public String getOperation() {
        return operation;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
