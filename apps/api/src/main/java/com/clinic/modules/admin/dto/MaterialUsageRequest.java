package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for recording material usage during a visit.
 */
public record MaterialUsageRequest(
        @NotNull(message = "Material ID is required")
        Long materialId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
        BigDecimal quantity,

        String notes
) {
}
