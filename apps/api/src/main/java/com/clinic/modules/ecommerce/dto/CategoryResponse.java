package com.clinic.modules.ecommerce.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for category information.
 */
public record CategoryResponse(
        Long id,
        Long parentId,
        String name,
        String slug,
        String description,
        Integer sortOrder,
        Boolean isActive,
        String fullPath,
        Integer depth,
        Instant createdAt,
        Instant updatedAt,
        List<CategoryResponse> children
) {
}