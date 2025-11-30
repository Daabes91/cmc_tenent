package com.clinic.modules.admin.auth;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property-based test for consistent success messages.
 * 
 * **Feature: admin-forgot-password, Property 5: Consistent success message**
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * This test verifies that:
 * 1. For any forgot password request (whether email exists or not), the system 
 *    displays the same success message to prevent user enumeration
 * 2. The response format and status code are consistent
 * 3. No information is leaked about whether the email exists
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ConsistentResponsePropertyTest {

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

    /**
     * Property 5: Consistent success message
     * 
     * For any forgot password request (whether email exists or not), the system 
     * should display the same success message to prevent user enumeration.
     * 
     * **Feature: admin-forgot-password, Property 5: Consistent success message**
     * **Validates: Requirements 2.1, 2.2, 2.3**
     */
    @Test
    public void forgotPasswordReturnsConsistentMessageForAllRequests() throws Exception {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create tenant
                TenantEntity tenant = createTestTenant();
                String tenantSlug = tenant.getSlug();
                
                // Create staff user in tenant
                StaffUser staff = createTestStaff(tenant);
                String existingEmail = staff.getEmail();
                
                // Generate non-existent email
                String nonExistentEmail = "nonexistent-" + UUID.randomUUID() + "@test.com";
                
                // Clear rate limits
                passwordResetService.clearRateLimit(existingEmail, tenantSlug);
                passwordResetService.clearRateLimit(nonExistentEmail, tenantSlug);
                
                // Request password reset for existing email
                Map<String, String> existingEmailRequest = Map.of(
                    "email", existingEmail,
                    "tenantSlug", tenantSlug
                );
                
                MvcResult existingResult = mockMvc.perform(post("/admin/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingEmailRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
                
                String existingResponse = existingResult.getResponse().getContentAsString();
                Map<String, String> existingResponseMap = objectMapper.readValue(existingResponse, Map.class);
                
                // Request password reset for non-existent email
                Map<String, String> nonExistentEmailRequest = Map.of(
                    "email", nonExistentEmail,
                    "tenantSlug", tenantSlug
                );
                
                MvcResult nonExistentResult = mockMvc.perform(post("/admin/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentEmailRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
                
                String nonExistentResponse = nonExistentResult.getResponse().getContentAsString();
                Map<String, String> nonExistentResponseMap = objectMapper.readValue(nonExistentResponse, Map.class);
                
                // Verify both responses have the same status code
                assertEquals(existingResult.getResponse().getStatus(), 
                    nonExistentResult.getResponse().getStatus(), 
                    "Status codes should be identical for existing and non-existent emails");
                
                // Verify both responses have the same message
                assertTrue(existingResponseMap.containsKey("message"), 
                    "Response for existing email should contain message");
                assertTrue(nonExistentResponseMap.containsKey("message"), 
                    "Response for non-existent email should contain message");
                
                assertEquals(existingResponseMap.get("message"), 
                    nonExistentResponseMap.get("message"), 
                    "Messages should be identical for existing and non-existent emails");
                
                // Verify message is generic and doesn't reveal email existence
                String message = existingResponseMap.get("message");
                assertFalse(message.toLowerCase().contains("found"), 
                    "Message should not contain 'found'");
                assertFalse(message.toLowerCase().contains("not found"), 
                    "Message should not contain 'not found'");
                assertFalse(message.toLowerCase().contains("exists"), 
                    "Message should not contain 'exists'");
                assertFalse(message.toLowerCase().contains("doesn't exist"), 
                    "Message should not contain 'doesn't exist'");
                assertFalse(message.toLowerCase().contains("invalid"), 
                    "Message should not contain 'invalid'");
                
                // Verify message is positive and helpful
                assertTrue(message.toLowerCase().contains("if") || message.toLowerCase().contains("will"), 
                    "Message should be conditional or future-tense");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 5 (variant): Consistent response for invalid tenant
     * 
     * For any forgot password request with an invalid tenant slug, the system 
     * should return the same success message.
     * 
     * **Feature: admin-forgot-password, Property 5: Consistent success message**
     * **Validates: Requirements 2.1, 2.2, 2.3**
     */
    @Test
    public void forgotPasswordReturnsConsistentMessageForInvalidTenant() throws Exception {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create tenant
                TenantEntity tenant = createTestTenant();
                
                // Create staff user
                StaffUser staff = createTestStaff(tenant);
                String email = staff.getEmail();
                
                // Generate invalid tenant slug
                String invalidTenantSlug = "invalid-" + UUID.randomUUID().toString().substring(0, 8);
                
                // Clear rate limits
                passwordResetService.clearRateLimit(email, tenant.getSlug());
                passwordResetService.clearRateLimit(email, invalidTenantSlug);
                
                // Request with valid tenant
                Map<String, String> validRequest = Map.of(
                    "email", email,
                    "tenantSlug", tenant.getSlug()
                );
                
                MvcResult validResult = mockMvc.perform(post("/admin/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
                
                String validResponse = validResult.getResponse().getContentAsString();
                Map<String, String> validResponseMap = objectMapper.readValue(validResponse, Map.class);
                
                // Request with invalid tenant
                Map<String, String> invalidRequest = Map.of(
                    "email", email,
                    "tenantSlug", invalidTenantSlug
                );
                
                MvcResult invalidResult = mockMvc.perform(post("/admin/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
                
                String invalidResponse = invalidResult.getResponse().getContentAsString();
                Map<String, String> invalidResponseMap = objectMapper.readValue(invalidResponse, Map.class);
                
                // Verify both responses are identical
                assertEquals(validResult.getResponse().getStatus(), 
                    invalidResult.getResponse().getStatus(), 
                    "Status codes should be identical for valid and invalid tenant");
                
                assertEquals(validResponseMap.get("message"), 
                    invalidResponseMap.get("message"), 
                    "Messages should be identical for valid and invalid tenant");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    /**
     * Property 5 (variant): Consistent response timing
     * 
     * For any forgot password request, the response time should be similar
     * regardless of whether the email exists (to prevent timing attacks).
     * 
     * Note: This is a simplified test - in production, more sophisticated
     * timing analysis would be needed.
     * 
     * **Feature: admin-forgot-password, Property 5: Consistent success message**
     * **Validates: Requirements 2.1, 2.2, 2.3**
     */
    @Test
    public void forgotPasswordResponseTimingIsConsistent() throws Exception {
        // Run property test with 30 iterations
        int tries = 30;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create tenant
                TenantEntity tenant = createTestTenant();
                
                // Create staff user
                StaffUser staff = createTestStaff(tenant);
                String existingEmail = staff.getEmail();
                String nonExistentEmail = "nonexistent-" + UUID.randomUUID() + "@test.com";
                
                // Clear rate limits
                passwordResetService.clearRateLimit(existingEmail, tenant.getSlug());
                passwordResetService.clearRateLimit(nonExistentEmail, tenant.getSlug());
                
                // Measure time for existing email
                Map<String, String> existingRequest = Map.of(
                    "email", existingEmail,
                    "tenantSlug", tenant.getSlug()
                );
                
                long startExisting = System.currentTimeMillis();
                mockMvc.perform(post("/admin/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                    .andExpect(status().isOk());
                long endExisting = System.currentTimeMillis();
                long existingDuration = endExisting - startExisting;
                
                // Measure time for non-existent email
                Map<String, String> nonExistentRequest = Map.of(
                    "email", nonExistentEmail,
                    "tenantSlug", tenant.getSlug()
                );
                
                long startNonExistent = System.currentTimeMillis();
                mockMvc.perform(post("/admin/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentRequest)))
                    .andExpect(status().isOk());
                long endNonExistent = System.currentTimeMillis();
                long nonExistentDuration = endNonExistent - startNonExistent;
                
                // Verify timing difference is not too large (within 500ms)
                // This is a loose check - in production, more sophisticated analysis needed
                long timingDifference = Math.abs(existingDuration - nonExistentDuration);
                assertTrue(timingDifference < 500, 
                    "Timing difference should be less than 500ms (was " + timingDifference + "ms)");
                
            } catch (AssertionError e) {
                failures.add("Try " + (i + 1) + " failed: " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Property test failed in " + failures.size() + " out of " + tries + " tries:\n" + 
                String.join("\n", failures));
        }
    }

    // ========== Helper Methods for Generating Random Test Data ==========

    private TenantEntity createTestTenant() {
        String slug = "tenant-" + UUID.randomUUID().toString().substring(0, 8);
        String name = "Test Tenant " + UUID.randomUUID().toString().substring(0, 8);
        TenantEntity tenant = new TenantEntity(slug, name);
        return tenantRepository.saveAndFlush(tenant);
    }

    private StaffUser createTestStaff(TenantEntity tenant) {
        String email = "staff-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String fullName = "Test Staff " + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = "$2a$10$" + UUID.randomUUID().toString().replace("-", "");
        
        StaffUser staff = new StaffUser(email, fullName, StaffRole.RECEPTIONIST, 
            passwordHash, StaffStatus.ACTIVE);
        staff.setTenant(tenant);
        
        return staffUserRepository.saveAndFlush(staff);
    }
}
