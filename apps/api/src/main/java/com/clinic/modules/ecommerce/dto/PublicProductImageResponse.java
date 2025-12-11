package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.ProductImageEntity;

import java.time.Instant;

/**
 * DTO for public product image API responses.
 * 
 * Contains image information suitable for customer-facing display.
 */
public record PublicProductImageResponse(
    Long id,
    String imageUrl,
    String altText,
    Integer sortOrder,
    boolean isMain,
    Instant createdAt,
    Instant updatedAt
) {
    
    /**
     * Creates a PublicProductImageResponse from a ProductImageEntity.
     * 
     * @param image the image entity
     * @return the response DTO
     */
    public static PublicProductImageResponse fromEntity(ProductImageEntity image) {
        return new PublicProductImageResponse(
            image.getId(),
            image.getImageUrl(),
            image.getEffectiveAltText(),
            image.getSortOrder(),
            image.isMain(),
            image.getCreatedAt(),
            image.getUpdatedAt()
        );
    }
}