package com.clinic.modules.saas.dto;

import com.clinic.modules.core.tenant.TenantStatus;

import java.time.Instant;

/**
 * Response DTO for tenant information
 * Includes all tenant fields including soft delete timestamp
 */
public record TenantResponse(
        Long id,
        String slug,
        String name,
        String customDomain,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
