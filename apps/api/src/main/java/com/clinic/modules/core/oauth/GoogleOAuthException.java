package com.clinic.modules.core.oauth;

/**
 * Exception thrown when Google OAuth operations fail.
 */
public class GoogleOAuthException extends RuntimeException {
    
    public GoogleOAuthException(String message) {
        super(message);
    }
    
    public GoogleOAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
