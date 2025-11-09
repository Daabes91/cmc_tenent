package com.clinic.modules.saas.dto;

import java.util.Map;

/**
 * Response DTO for analytics data
 */
public record AnalyticsResponse(
        Map<String, Object> tenantGrowth,
        Map<String, Object> userActivity,
        Map<String, Object> systemPerformance
) {
}
