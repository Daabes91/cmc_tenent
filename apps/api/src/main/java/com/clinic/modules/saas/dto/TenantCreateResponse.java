package com.clinic.modules.saas.dto;

import com.clinic.modules.core.tenant.TenantStatus;

import java.time.Instant;

/**
 * Response DTO for tenant creation
 * Includes the auto-generated admin credentials
 */
public record TenantCreateResponse(
        Long id,
        String slug,
        String name,
        String customDomain,
        TenantStatus status,
        Instant createdAt,
        AdminCredentials adminCredentials
) {
}
