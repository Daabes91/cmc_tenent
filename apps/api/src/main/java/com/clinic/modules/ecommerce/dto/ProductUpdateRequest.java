package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for updating an existing product.
 * All fields are optional - only provided fields will be updated.
 */
public record ProductUpdateRequest(
        
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,
        
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,
        
        @Size(max = 500, message = "Short description must not exceed 500 characters")
        String shortDescription,
        
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,
        
        @DecimalMin(value = "0.01", message = "Compare at price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Compare at price must have at most 8 integer digits and 2 decimal places")
        BigDecimal compareAtPrice,
        
        Boolean isTaxable,
        
        Boolean isVisible
) {
}