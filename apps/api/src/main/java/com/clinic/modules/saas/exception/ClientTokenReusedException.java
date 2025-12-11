package com.clinic.modules.saas.exception;

/**
 * Exception thrown when a client token is reused.
 * Client tokens should only be used once for security.
 */
public class ClientTokenReusedException extends RuntimeException {
    
    public ClientTokenReusedException(String message) {
        super(message);
    }
    
    public ClientTokenReusedException(String message, Throwable cause) {
        super(message, cause);
    }
}
