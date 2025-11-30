package com.clinic.modules.core.oauth;

/**
 * Data Transfer Object for Google OAuth token response.
 * Contains the tokens returned from Google's token exchange endpoint.
 */
public record GoogleTokenResponse(
    String accessToken,
    String refreshToken,
    String idToken,
    int expiresIn,
    String tokenType
) {}
