package com.clinic.modules.ecommerce.exception;

/**
 * Base exception for e-commerce operations.
 * All e-commerce specific exceptions should extend this class.
 */
public class EcommerceException extends RuntimeException {

    public EcommerceException(String message) {
        super(message);
    }

    public EcommerceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EcommerceException(Throwable cause) {
        super(cause);
    }
}