package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.ProductEntity;
import com.clinic.modules.ecommerce.model.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for public product API responses.
 * 
 * Contains product information suitable for customer-facing display.
 */
public record PublicProductResponse(
    Long id,
    String name,
    String slug,
    String description,
    String shortDescription,
    ProductStatus status,
    BigDecimal price,
    BigDecimal compareAtPrice,
    String currency,
    boolean hasVariants,
    boolean isTaxable,
    boolean isVisible,
    Instant createdAt,
    Instant updatedAt,
    List<PublicProductVariantResponse> variants,
    List<PublicProductImageResponse> images,
    List<PublicCategoryResponse> categories
) {
    
    /**
     * Creates a PublicProductResponse from a ProductEntity.
     * 
     * @param product the product entity
     * @return the response DTO
     */
    public static PublicProductResponse fromEntity(ProductEntity product) {
        return new PublicProductResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getDescription(),
            product.getShortDescription(),
            product.getStatus(),
            product.getPrice(),
            product.getCompareAtPrice(),
            product.getCurrency(),
            product.hasVariants(),
            product.isTaxable(),
            product.isVisible(),
            product.getCreatedAt(),
            product.getUpdatedAt(),
            product.getVariants().stream()
                .map(PublicProductVariantResponse::fromEntity)
                .toList(),
            product.getImages().stream()
                .sorted((a, b) -> {
                    // Main image first, then by sort order
                    if (a.isMain() && !b.isMain()) return -1;
                    if (!a.isMain() && b.isMain()) return 1;
                    return Integer.compare(a.getSortOrder(), b.getSortOrder());
                })
                .map(PublicProductImageResponse::fromEntity)
                .toList(),
            product.getProductCategories().stream()
                .map(pc -> PublicCategoryResponse.fromEntity(pc.getCategory()))
                .toList()
        );
    }
}