package com.clinic.modules.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for product variant information.
 */
public record ProductVariantResponse(
        Long id,
        Long productId,
        String sku,
        String name,
        BigDecimal price,
        BigDecimal compareAtPrice,
        String currency,
        Integer stockQuantity,
        Boolean isInStock,
        Instant createdAt,
        Instant updatedAt
) {
}