package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating category sort order.
 */
public record CategorySortOrderUpdateRequest(
        
        @NotNull(message = "Sort order is required")
        @Min(value = 0, message = "Sort order cannot be negative")
        Integer sortOrder
) {
}