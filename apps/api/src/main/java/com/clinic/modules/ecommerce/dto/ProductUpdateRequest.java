package com.clinic.modules.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.clinic.modules.ecommerce.model.ProductStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for updating an existing product.
 * All fields are optional - only provided fields will be updated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductUpdateRequest(
        
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,

        @Size(max = 255, message = "Arabic product name must not exceed 255 characters")
        String nameAr,

        @Size(max = 255, message = "Product slug must not exceed 255 characters")
        String slug,
        
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
        
        Boolean isTaxable,
        
        Boolean isVisible,

        @Size(max = 10, message = "Currency must not exceed 10 characters")
        String currency,

        ProductStatus status,

        List<Long> categoryIds
) {
}
