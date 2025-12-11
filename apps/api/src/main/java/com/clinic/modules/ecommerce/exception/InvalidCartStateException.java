package com.clinic.modules.ecommerce.exception;

/**
 * Exception thrown when cart operations fail validation.
 * Used for cart state management and item operations.
 */
public class InvalidCartStateException extends EcommerceException {

    private final String cartId;
    private final String operation;

    public InvalidCartStateException(String message) {
        super(message);
        this.cartId = null;
        this.operation = null;
    }

    public InvalidCartStateException(String message, Throwable cause) {
        super(message, cause);
        this.cartId = null;
        this.operation = null;
    }

    public InvalidCartStateException(String cartId, String operation, String message) {
        super(message);
        this.cartId = cartId;
        this.operation = operation;
    }

    public InvalidCartStateException(String cartId, String operation, String message, Throwable cause) {
        super(message, cause);
        this.cartId = cartId;
        this.operation = operation;
    }

    public String getCartId() {
        return cartId;
    }

    public String getOperation() {
        return operation;
    }
}