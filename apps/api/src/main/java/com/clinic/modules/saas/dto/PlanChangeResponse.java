package com.clinic.modules.saas.dto;

import com.clinic.modules.saas.model.PlanTier;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for plan change operations (upgrade/downgrade).
 * Contains approval URL for upgrades and effective date information.
 */
public record PlanChangeResponse(
        String approvalUrl,
        PlanTier newTier,
        LocalDateTime effectiveDate,
        BigDecimal newPrice,
        String currency,
        String message
) {
}
