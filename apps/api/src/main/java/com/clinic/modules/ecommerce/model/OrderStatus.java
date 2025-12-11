package com.clinic.modules.ecommerce.model;

/**
 * Order status enumeration for tracking order lifecycle.
 */
public enum OrderStatus {
    PENDING_PAYMENT("Pending Payment"),
    PAID("Paid"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}