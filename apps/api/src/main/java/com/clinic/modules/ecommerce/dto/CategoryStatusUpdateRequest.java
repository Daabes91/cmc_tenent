package com.clinic.modules.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating category status.
 */
public record CategoryStatusUpdateRequest(
        
        @NotNull(message = "Category active status is required")
        Boolean isActive
) {
}