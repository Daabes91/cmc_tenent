package com.clinic.modules.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding items to cart.
 * Used in public cart API endpoints.
 */
public class AddToCartRequest {

    @JsonProperty("product_id")
    @NotNull(message = "Product ID is required")
    private Long productId;

    @JsonProperty("variant_id")
    private Long variantId;

    @JsonProperty("quantity")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Constructors
    public AddToCartRequest() {}

    public AddToCartRequest(Long productId, Long variantId, Integer quantity) {
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "AddToCartRequest{" +
                "productId=" + productId +
                ", variantId=" + variantId +
                ", quantity=" + quantity +
                '}';
    }
}