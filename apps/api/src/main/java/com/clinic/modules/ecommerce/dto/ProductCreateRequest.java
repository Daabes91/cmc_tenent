package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a new product.
 */
public record ProductCreateRequest(
        
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,

        @Size(max = 255, message = "Arabic product name must not exceed 255 characters")
        String nameAr,
        
        @NotBlank(message = "Product slug is required")
        @Size(max = 255, message = "Product slug must not exceed 255 characters")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Product slug must contain only lowercase letters, numbers, and hyphens")
        String slug,
        
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        String sku,
        
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @Size(max = 5000, message = "Arabic description must not exceed 5000 characters")
        String descriptionAr,
        
        @Size(max = 500, message = "Short description must not exceed 500 characters")
        String shortDescription,

        @Size(max = 500, message = "Arabic short description must not exceed 500 characters")
        String shortDescriptionAr,
        
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,
        
        @DecimalMin(value = "0.01", message = "Compare at price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Compare at price must have at most 8 integer digits and 2 decimal places")
        BigDecimal compareAtPrice,
        
        @Size(max = 3, message = "Currency must not exceed 3 characters")
        String currency,
        
        Boolean isTaxable,
        
        Boolean isVisible,

        // Optional list of image URLs to attach to the product
        List<@Size(max = 2048, message = "Image URL must not exceed 2048 characters") String> images
) {
}
