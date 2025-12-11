package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.OrderItemEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for order items.
 */
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String currency;
    private String displayName;
    private LocalDateTime createdAt;

    // Constructors
    public OrderItemResponse() {}

    public OrderItemResponse(OrderItemEntity entity) {
        this.id = entity.getId();
        this.productId = entity.getProduct() != null ? entity.getProduct().getId() : null;
        this.variantId = entity.getVariant() != null ? entity.getVariant().getId() : null;
        this.productName = entity.getProductName();
        this.variantName = entity.getVariantName();
        this.sku = entity.getSku();
        this.quantity = entity.getQuantity();
        this.unitPrice = entity.getUnitPrice();
        this.totalPrice = entity.getTotalPrice();
        this.currency = entity.getCurrency();
        this.displayName = entity.getDisplayName();
        this.createdAt = entity.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderItemResponse{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", variantName='" + variantName + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", currency='" + currency + '\'' +
                '}';
    }
}