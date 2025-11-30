package com.clinic.modules.admin.dto;

import java.time.Instant;

/**
 * Response DTO for expense category.
 * Includes category details with system and active status flags.
 */
public record ExpenseCategoryResponse(
        Long id,
        String name,
        Boolean isSystem,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
