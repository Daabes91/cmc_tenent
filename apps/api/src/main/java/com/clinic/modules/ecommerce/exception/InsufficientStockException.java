package com.clinic.modules.ecommerce.exception;

/**
 * Exception thrown when requested quantity exceeds available stock.
 * Used in cart operations and order processing.
 */
public class InsufficientStockException extends EcommerceException {

    private final Long productId;
    private final Long variantId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.variantId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
        this.variantId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public InsufficientStockException(Long productId, Long variantId, 
                                    Integer requestedQuantity, Integer availableQuantity, 
                                    String message) {
        super(message);
        this.productId = productId;
        this.variantId = variantId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(Long productId, Long variantId, 
                                    Integer requestedQuantity, Integer availableQuantity, 
                                    String message, Throwable cause) {
        super(message, cause);
        this.productId = productId;
        this.variantId = variantId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}