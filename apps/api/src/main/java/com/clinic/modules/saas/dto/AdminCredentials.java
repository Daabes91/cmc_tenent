package com.clinic.modules.saas.dto;

/**
 * Nested DTO containing auto-generated admin credentials
 * This is only returned once during tenant creation
 */
public record AdminCredentials(
        String email,
        String password
) {
}
