package com.clinic.modules.saas.dto;

import java.time.Instant;

/**
 * Response DTO for SAAS Manager login
 * Contains JWT token and manager details
 */
public record SaasLoginResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        Long managerId,
        String email,
        String fullName
) {
    /**
     * Factory method to create a Bearer token response
     */
    public static SaasLoginResponse bearer(
            String accessToken,
            Instant expiresAt,
            Long managerId,
            String email,
            String fullName
    ) {
        return new SaasLoginResponse("Bearer", accessToken, expiresAt, managerId, email, fullName);
    }
}
