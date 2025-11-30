package com.clinic.modules.core.oauth;

/**
 * Data Transfer Object for Google user information.
 * Contains the user profile data returned from Google's UserInfo API.
 */
public record GoogleUserInfo(
    String id,              // Google user ID
    String email,
    boolean emailVerified,
    String name,
    String givenName,
    String familyName,
    String picture,
    String locale
) {}
