package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for material usage details with currency conversion.
 */
public record MaterialUsageResponse(
        Long id,
        Long materialId,
        String materialName,
        BigDecimal quantity,
        BigDecimal unitCost,
        String currency,
        BigDecimal totalCost,
        BigDecimal convertedTotalCost,
        String convertedCurrency,
        String notes,
        Instant createdAt
) {
}
