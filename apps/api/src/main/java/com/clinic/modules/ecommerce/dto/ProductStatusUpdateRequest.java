package com.clinic.modules.ecommerce.dto;

import com.clinic.modules.ecommerce.model.ProductStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating product status.
 */
public record ProductStatusUpdateRequest(
        
        @NotNull(message = "Product status is required")
        ProductStatus status
) {
}