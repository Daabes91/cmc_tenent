package com.clinic.modules.saas.dto;

import java.time.Instant;

/**
 * Response DTO for per-tenant usage metrics shown in the SaaS admin panel.
 */
public record TenantMetricsResponse(
        Long tenantId,
        long userCount,
        long staffCount,
        long patientCount,
        long appointmentCount,
        double storageUsedMB,
        Instant lastActivityAt
) {
}
