package com.clinic.modules.saas.dto;

/**
 * Response DTO for recent activity entries
 */
public record ActivityResponse(
        String id,
        String timestamp,
        String type,  // tenant_created, tenant_updated, tenant_deleted
        String description,
        String managerName
) {
}
