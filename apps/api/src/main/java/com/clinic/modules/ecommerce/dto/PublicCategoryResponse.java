package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.CategoryEntity;

import java.time.Instant;

/**
 * DTO for public category API responses.
 * 
 * Contains category information suitable for customer-facing display.
 */
public record PublicCategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    Integer sortOrder,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {
    
    /**
     * Creates a PublicCategoryResponse from a CategoryEntity.
     * 
     * @param category the category entity
     * @return the response DTO
     */
    public static PublicCategoryResponse fromEntity(CategoryEntity category) {
        return new PublicCategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.getSortOrder(),
            category.isActive(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}