package com.clinic.modules.saas.exception;

/**
 * Exception thrown when authentication fails or credentials are invalid.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
