package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for material catalog entry.
 * Includes both original price (in original currency) and converted price (in clinic currency).
 */
public record MaterialCatalogResponse(
        Long id,
        String name,
        String description,
        BigDecimal unitCost,        // Original price
        String currency,            // Original currency
        BigDecimal convertedPrice,  // Price in clinic currency
        String convertedCurrency,   // Clinic currency
        String unitOfMeasure,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
