package com.clinic.modules.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new tenant
 */
public record TenantCreateRequest(
        @NotBlank(message = "Slug is required")
        @Pattern(
                regexp = "^[a-z0-9-]{3,64}$",
                message = "Slug must be between 3 and 64 characters and contain only lowercase letters, numbers, and hyphens"
        )
        String slug,

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @Pattern(
                regexp = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$",
                message = "Custom domain must be a valid domain name format"
        )
        @Size(max = 255, message = "Custom domain must not exceed 255 characters")
        String customDomain
) {
}
