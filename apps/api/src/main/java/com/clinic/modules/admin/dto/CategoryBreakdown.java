package com.clinic.modules.admin.dto;

/**
 * Represents a categorical metric (e.g. by doctor, service, payment method) with percentage share.
 *
 * @param label      Display name for the category.
 * @param value      Count for the category.
 * @param percentage Share in percentage (0-100) for chart visualisations.
 */
public record CategoryBreakdown(
        String label,
        long value,
        double percentage
) {
}
