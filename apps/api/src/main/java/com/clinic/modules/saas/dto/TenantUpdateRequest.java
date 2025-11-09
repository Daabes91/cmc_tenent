package com.clinic.modules.saas.dto;

import com.clinic.modules.core.tenant.TenantStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing tenant
 * Note: Slug cannot be modified after creation
 */
public record TenantUpdateRequest(
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @Pattern(
                regexp = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$",
                message = "Custom domain must be a valid domain name format"
        )
        @Size(max = 255, message = "Custom domain must not exceed 255 characters")
        String customDomain,

        TenantStatus status
) {
}
