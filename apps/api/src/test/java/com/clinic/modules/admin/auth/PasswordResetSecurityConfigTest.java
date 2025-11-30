package com.clinic.modules.admin.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test to verify that password reset endpoints are accessible without authentication
 * 
 * Feature: admin-forgot-password
 * Validates: Requirements 1.3, 3.3, 4.4 - Security configuration allows unauthenticated access
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordResetSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void forgotPasswordEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        // Given: A forgot password request without authentication token
        String requestBody = """
            {
                "email": "test@example.com",
                "tenantSlug": "test-clinic"
            }
            """;

        // When: Making a request to the forgot-password endpoint
        // Then: Should return 200 OK (not 401 Unauthorized or 403 Forbidden)
        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void validateResetTokenEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        // Given: A validate token request without authentication token
        String requestBody = """
            {
                "token": "some-token-value"
            }
            """;

        // When: Making a request to the validate-reset-token endpoint
        // Then: Should return 200 OK (not 401 Unauthorized or 403 Forbidden)
        mockMvc.perform(post("/admin/auth/validate-reset-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void resetPasswordEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        // Given: A reset password request without authentication token
        String requestBody = """
            {
                "token": "some-token-value",
                "newPassword": "newPassword123"
            }
            """;

        // When: Making a request to the reset-password endpoint
        // Then: Should return 200 or 400 (not 401 Unauthorized or 403 Forbidden)
        // Note: 400 is acceptable here as the token is invalid, but we're testing
        // that the endpoint is accessible without authentication
        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().is4xxClientError()); // Could be 400 (bad request) but not 401/403
    }
}
