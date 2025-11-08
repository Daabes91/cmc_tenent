package com.clinic.modules.saas.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing SAAS Manager JWT token
 * Note: This is optional and may not be implemented initially
 */
public record SaasRefreshRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}
