package com.clinic.modules.admin.dto;

/**
 * Represents a point in a time series for charts.
 *
 * @param period Period label already formatted for display (e.g. "Apr 12").
 * @param value  Numeric value for the period.
 */
public record TimeSeriesPoint(
        String period,
        long value
) {
}
