package com.clinic.modules.ecommerce.exception;

/**
 * Exception thrown when authentication fails in e-commerce operations.
 * Used for JWT token validation, API key validation, and other authentication failures.
 */
public class AuthenticationException extends EcommerceException {

    private final String authenticationType;
    private final String reason;

    public AuthenticationException(String message) {
        super(message);
        this.authenticationType = "UNKNOWN";
        this.reason = null;
    }

    public AuthenticationException(String authenticationType, String reason, String message) {
        super(message);
        this.authenticationType = authenticationType;
        this.reason = reason;
    }

    public AuthenticationException(String authenticationType, String reason, String message, Throwable cause) {
        super(message, cause);
        this.authenticationType = authenticationType;
        this.reason = reason;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getReason() {
        return reason;
    }
}