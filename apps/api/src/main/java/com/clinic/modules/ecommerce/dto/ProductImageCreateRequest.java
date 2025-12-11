package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a new product image.
 */
public record ProductImageCreateRequest(
        
        @NotBlank(message = "Image URL is required")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl,
        
        @Size(max = 255, message = "Alt text must not exceed 255 characters")
        String altText,
        
        @Min(value = 0, message = "Sort order cannot be negative")
        Integer sortOrder,
        
        Boolean isMain
) {
}