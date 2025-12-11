package com.clinic.modules.ecommerce.dto;

import java.time.Instant;

/**
 * Response DTO for product image information.
 */
public record ProductImageResponse(
        Long id,
        Long productId,
        String imageUrl,
        String altText,
        Integer sortOrder,
        Boolean isMain,
        Instant createdAt,
        Instant updatedAt
) {
}