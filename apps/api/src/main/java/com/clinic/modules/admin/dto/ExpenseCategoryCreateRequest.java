package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new expense category.
 * Only the name is required; is_system and is_active are set by the service.
 */
public record ExpenseCategoryCreateRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 255, message = "Category name must not exceed 255 characters")
        String name
) {
}
