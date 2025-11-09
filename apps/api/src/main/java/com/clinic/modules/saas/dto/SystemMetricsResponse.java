package com.clinic.modules.saas.dto;

import java.util.List;

/**
 * Response DTO for system-wide metrics
 */
public record SystemMetricsResponse(
        long totalTenants,
        long activeTenants,
        long totalUsers,
        long activeUsers,
        long apiResponseTime,
        String databaseStatus,
        List<ActivityResponse> recentActivity
) {
}
