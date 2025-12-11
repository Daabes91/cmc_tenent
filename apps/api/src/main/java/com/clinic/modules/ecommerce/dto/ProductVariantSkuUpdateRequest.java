package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating product variant SKU.
 */
public record ProductVariantSkuUpdateRequest(
        
        @NotBlank(message = "Variant SKU is required")
        @Size(max = 100, message = "Variant SKU must not exceed 100 characters")
        String sku
) {
}