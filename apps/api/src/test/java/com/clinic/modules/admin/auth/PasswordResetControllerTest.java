package com.clinic.modules.admin.auth;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PasswordResetController REST endpoints.
 * 
 * Tests the three main endpoints:
 * - POST /admin/auth/forgot-password
 * - POST /admin/auth/validate-reset-token
 * - POST /admin/auth/reset-password
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TenantEntity testTenant;
    private StaffUser testStaff;

    @BeforeEach
    public void setup() {
        // Create test tenant
        String slug = "test-tenant-" + UUID.randomUUID().toString().substring(0, 8);
        String name = "Test Tenant";
        testTenant = new TenantEntity(slug, name);
        testTenant = tenantRepository.saveAndFlush(testTenant);

        // Create test staff user
        String email = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String fullName = "Test Staff";
        String passwordHash = "$2a$10$" + UUID.randomUUID().toString().replace("-", "");
        
        testStaff = new StaffUser(email, fullName, StaffRole.RECEPTIONIST, 
            passwordHash, StaffStatus.ACTIVE);
        testStaff.setTenant(testTenant);
        testStaff = staffUserRepository.saveAndFlush(testStaff);
    }

    // ========== Tests for POST /admin/auth/forgot-password ==========

    @Test
    public void forgotPassword_WithValidEmailAndTenant_ReturnsSuccess() throws Exception {
        // Clear rate limit
        passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());

        Map<String, String> request = Map.of(
            "email", testStaff.getEmail(),
            "tenantSlug", testTenant.getSlug()
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    public void forgotPassword_WithNonExistentEmail_ReturnsSuccessMessage() throws Exception {
        String nonExistentEmail = "nonexistent-" + UUID.randomUUID() + "@test.com";
        passwordResetService.clearRateLimit(nonExistentEmail, testTenant.getSlug());

        Map<String, String> request = Map.of(
            "email", nonExistentEmail,
            "tenantSlug", testTenant.getSlug()
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void forgotPassword_WithInvalidTenant_ReturnsSuccessMessage() throws Exception {
        String invalidTenant = "invalid-tenant";
        passwordResetService.clearRateLimit(testStaff.getEmail(), invalidTenant);

        Map<String, String> request = Map.of(
            "email", testStaff.getEmail(),
            "tenantSlug", invalidTenant
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void forgotPassword_WithMissingEmail_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "tenantSlug", testTenant.getSlug()
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void forgotPassword_WithMissingTenantSlug_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "email", testStaff.getEmail()
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void forgotPassword_WithInvalidEmailFormat_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "email", "not-an-email",
            "tenantSlug", testTenant.getSlug()
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void forgotPassword_WithEmptyEmail_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "email", "",
            "tenantSlug", testTenant.getSlug()
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void forgotPassword_WithEmptyTenantSlug_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "email", testStaff.getEmail(),
            "tenantSlug", ""
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void forgotPassword_WithLanguageHeader_ReturnsSuccess() throws Exception {
        passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());

        Map<String, String> request = Map.of(
            "email", testStaff.getEmail(),
            "tenantSlug", testTenant.getSlug()
        );

        mockMvc.perform(post("/admin/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "ar")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    // ========== Tests for POST /admin/auth/validate-reset-token ==========

    @Test
    public void validateResetToken_WithValidToken_ReturnsValid() throws Exception {
        // Generate a valid token
        passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
        Optional<String> token = passwordResetService.requestPasswordResetForTesting(
            testStaff.getEmail(), testTenant.getSlug()
        );
        assertTrue(token.isPresent());

        Map<String, String> request = Map.of(
            "token", token.get()
        );

        mockMvc.perform(post("/admin/auth/validate-reset-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void validateResetToken_WithInvalidToken_ReturnsInvalid() throws Exception {
        String invalidToken = "invalid-token-" + UUID.randomUUID();

        Map<String, String> request = Map.of(
            "token", invalidToken
        );

        mockMvc.perform(post("/admin/auth/validate-reset-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void validateResetToken_WithMissingToken_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of();

        mockMvc.perform(post("/admin/auth/validate-reset-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void validateResetToken_WithEmptyToken_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "token", ""
        );

        mockMvc.perform(post("/admin/auth/validate-reset-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ========== Tests for POST /admin/auth/reset-password ==========

    @Test
    public void resetPassword_WithValidToken_ReturnsSuccess() throws Exception {
        // Generate a valid token
        passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
        Optional<String> token = passwordResetService.requestPasswordResetForTesting(
            testStaff.getEmail(), testTenant.getSlug()
        );
        assertTrue(token.isPresent());

        Map<String, String> request = Map.of(
            "token", token.get(),
            "newPassword", "NewPassword123!"
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(containsString("success")));
    }

    @Test
    public void resetPassword_WithInvalidToken_ReturnsBadRequest() throws Exception {
        String invalidToken = "invalid-token-" + UUID.randomUUID();

        Map<String, String> request = Map.of(
            "token", invalidToken,
            "newPassword", "NewPassword123!"
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Invalid")));
    }

    @Test
    public void resetPassword_WithUsedToken_ReturnsBadRequest() throws Exception {
        // Generate and use a token
        passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
        Optional<String> token = passwordResetService.requestPasswordResetForTesting(
            testStaff.getEmail(), testTenant.getSlug()
        );
        assertTrue(token.isPresent());

        // Use the token once
        boolean firstReset = passwordResetService.resetPassword(token.get(), "FirstPassword123!");
        assertTrue(firstReset);

        // Try to use it again
        Map<String, String> request = Map.of(
            "token", token.get(),
            "newPassword", "SecondPassword123!"
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Invalid")));
    }

    @Test
    public void resetPassword_WithMissingToken_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "newPassword", "NewPassword123!"
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void resetPassword_WithMissingPassword_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "token", "some-token"
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void resetPassword_WithShortPassword_ReturnsBadRequest() throws Exception {
        // Generate a valid token
        passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
        Optional<String> token = passwordResetService.requestPasswordResetForTesting(
            testStaff.getEmail(), testTenant.getSlug()
        );
        assertTrue(token.isPresent());

        Map<String, String> request = Map.of(
            "token", token.get(),
            "newPassword", "short"  // Less than 8 characters
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void resetPassword_WithEmptyPassword_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of(
            "token", "some-token",
            "newPassword", ""
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void resetPassword_UpdatesPasswordInDatabase() throws Exception {
        // Get original password hash
        String originalPasswordHash = testStaff.getPasswordHash();

        // Generate a valid token
        passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
        Optional<String> token = passwordResetService.requestPasswordResetForTesting(
            testStaff.getEmail(), testTenant.getSlug()
        );
        assertTrue(token.isPresent());

        // Reset password
        Map<String, String> request = Map.of(
            "token", token.get(),
            "newPassword", "NewPassword123!"
        );

        mockMvc.perform(post("/admin/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Verify password was updated
        StaffUser updatedStaff = staffUserRepository.findById(testStaff.getId()).orElseThrow();
        assertNotEquals(originalPasswordHash, updatedStaff.getPasswordHash(), 
            "Password hash should be updated");
    }
}
