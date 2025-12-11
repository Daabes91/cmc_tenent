package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for updating an existing product variant.
 * All fields are optional - only provided fields will be updated.
 */
public record ProductVariantUpdateRequest(
        
        @Size(max = 255, message = "Variant name must not exceed 255 characters")
        String name,
        
        @DecimalMin(value = "0.01", message = "Variant price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Variant price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,
        
        @DecimalMin(value = "0.01", message = "Compare at price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Compare at price must have at most 8 integer digits and 2 decimal places")
        BigDecimal compareAtPrice,
        
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity
) {
}