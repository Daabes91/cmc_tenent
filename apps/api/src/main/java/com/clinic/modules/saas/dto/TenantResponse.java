package com.clinic.modules.saas.dto;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantStatus;
import com.clinic.modules.saas.model.PlanTier;

import java.time.Instant;

/**
 * Response DTO for tenant information
 * Includes all tenant fields including soft delete timestamp, billing status, and plan tier
 */
public record TenantResponse(
        Long id,
        String slug,
        String name,
        String customDomain,
        TenantStatus status,
        BillingStatus billingStatus,
        PlanTier planTier,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
