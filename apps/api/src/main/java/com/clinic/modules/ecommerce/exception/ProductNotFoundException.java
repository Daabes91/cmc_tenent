package com.clinic.modules.ecommerce.exception;

/**
 * Exception thrown when products or variants are not found.
 * Used in product lookup operations across the e-commerce module.
 */
public class ProductNotFoundException extends EcommerceException {

    private final Long productId;
    private final Long tenantId;

    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
        this.tenantId = null;
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
        this.tenantId = null;
    }

    public ProductNotFoundException(Long productId, Long tenantId) {
        super("Product " + productId + " not found for tenant " + tenantId);
        this.productId = productId;
        this.tenantId = tenantId;
    }

    public ProductNotFoundException(Long productId, Long tenantId, String message) {
        super(message);
        this.productId = productId;
        this.tenantId = tenantId;
    }

    public ProductNotFoundException(Long productId, Long tenantId, String message, Throwable cause) {
        super(message, cause);
        this.productId = productId;
        this.tenantId = tenantId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}