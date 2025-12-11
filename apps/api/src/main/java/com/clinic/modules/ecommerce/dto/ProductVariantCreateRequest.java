package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new product variant.
 */
public record ProductVariantCreateRequest(
        
        @NotBlank(message = "Variant SKU is required")
        @Size(max = 100, message = "Variant SKU must not exceed 100 characters")
        String sku,
        
        @NotBlank(message = "Variant name is required")
        @Size(max = 255, message = "Variant name must not exceed 255 characters")
        String name,
        
        @NotNull(message = "Variant price is required")
        @DecimalMin(value = "0.01", message = "Variant price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Variant price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,
        
        @DecimalMin(value = "0.01", message = "Compare at price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Compare at price must have at most 8 integer digits and 2 decimal places")
        BigDecimal compareAtPrice,
        
        @Size(max = 3, message = "Currency must not exceed 3 characters")
        String currency,
        
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity
) {
}