package com.clinic.modules.admin.dto;

import java.math.BigDecimal;

/**
 * Represents revenue totals for a labelled period (e.g. per month).
 *
 * @param period Label for the period (e.g. "Apr 2024").
 * @param amount Total revenue collected for the period.
 */
public record RevenuePoint(
        String period,
        BigDecimal amount
) {
}
