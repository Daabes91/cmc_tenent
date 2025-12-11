package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a new category.
 */
public record CategoryCreateRequest(
        
        @NotBlank(message = "Category name is required")
        @Size(max = 255, message = "Category name must not exceed 255 characters")
        String name,
        
        @NotBlank(message = "Category slug is required")
        @Size(max = 255, message = "Category slug must not exceed 255 characters")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Category slug must contain only lowercase letters, numbers, and hyphens")
        String slug,
        
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,
        
        Long parentId,
        
        @Min(value = 0, message = "Sort order cannot be negative")
        Integer sortOrder,
        
        Boolean isActive
) {
}