package com.clinic.modules.core.oauth;

/**
 * Exception thrown when an OAuth state token is invalid, expired, or already consumed.
 */
public class InvalidOAuthStateException extends RuntimeException {
    
    public InvalidOAuthStateException(String message) {
        super(message);
    }
    
    public InvalidOAuthStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
