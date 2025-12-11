package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating product SKU.
 */
public record ProductSkuUpdateRequest(
        
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        String sku
) {
}