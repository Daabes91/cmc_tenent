package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a material in the catalog.
 */
public record MaterialCatalogRequest(
        @NotBlank(message = "Material name is required")
        @Size(max = 200, message = "Material name must not exceed 200 characters")
        String name,

        String description,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.01", message = "Unit cost must be greater than 0")
        BigDecimal unitCost,

        @Size(max = 3, message = "Currency must be 3-letter code (e.g., USD, JOD)")
        String currency,

        @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
        String unitOfMeasure,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}
