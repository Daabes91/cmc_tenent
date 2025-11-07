package com.clinic.modules.admin.auth.dto;

import java.time.Instant;

public record AuthTokensResponse(
        String tokenType,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static AuthTokensResponse bearer(String accessToken,
                                            Instant accessTokenExpiresAt,
                                            String refreshToken,
                                            Instant refreshTokenExpiresAt) {
        return new AuthTokensResponse("Bearer", accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt);
    }
}
