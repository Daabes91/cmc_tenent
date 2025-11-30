package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an expense category.
 * Only the name can be updated; is_system and is_active are managed separately.
 */
public record ExpenseCategoryUpdateRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 255, message = "Category name must not exceed 255 characters")
        String name
) {
}
