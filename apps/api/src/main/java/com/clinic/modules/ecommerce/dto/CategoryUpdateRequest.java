package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for updating an existing category.
 * All fields are optional - only provided fields will be updated.
 */
public record CategoryUpdateRequest(
        
        @Size(max = 255, message = "Category name must not exceed 255 characters")
        String name,
        
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,
        
        Long parentId,
        
        @Min(value = 0, message = "Sort order cannot be negative")
        Integer sortOrder,
        
        Boolean isActive
) {
}