package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.ProductStatus;
import com.clinic.modules.ecommerce.model.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for product information.
 */
public record ProductResponse(
        Long id,
        String name,
        String slug,
        String sku,
        String description,
        String shortDescription,
        ProductStatus status,
        ProductType productType,
        BigDecimal price,
        BigDecimal compareAtPrice,
        String currency,
        Boolean hasVariants,
        Boolean isTaxable,
        Boolean isVisible,
        Instant createdAt,
        Instant updatedAt,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> images,
        List<CategoryResponse> categories
) {
}