package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.ProductVariantEntity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for public product variant API responses.
 * 
 * Contains variant information suitable for customer-facing display.
 */
public record PublicProductVariantResponse(
    Long id,
    String sku,
    String name,
    BigDecimal price,
    BigDecimal compareAtPrice,
    String currency,
    Integer stockQuantity,
    boolean isInStock,
    Instant createdAt,
    Instant updatedAt
) {
    
    /**
     * Creates a PublicProductVariantResponse from a ProductVariantEntity.
     * 
     * @param variant the variant entity
     * @return the response DTO
     */
    public static PublicProductVariantResponse fromEntity(ProductVariantEntity variant) {
        return new PublicProductVariantResponse(
            variant.getId(),
            variant.getSku(),
            variant.getName(),
            variant.getPrice(),
            variant.getCompareAtPrice(),
            variant.getCurrency(),
            variant.getStockQuantity(),
            variant.isInStock(),
            variant.getCreatedAt(),
            variant.getUpdatedAt()
        );
    }
}