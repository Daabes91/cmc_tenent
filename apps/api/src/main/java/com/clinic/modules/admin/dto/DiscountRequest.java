package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for applying a discount to a treatment plan.
 */
public record DiscountRequest(
        @NotNull(message = "Discount amount is required")
        @DecimalMin(value = "0.01", message = "Discount amount must be greater than 0")
        BigDecimal discountAmount,

        @NotBlank(message = "Discount reason is required")
        String discountReason
) {
}
