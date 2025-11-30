package com.clinic.modules.admin.auth;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: admin-forgot-password, Property 23: Tenant slug requirement
 * 
 * Property: For any password reset request, the system should require a tenant slug 
 * and reject requests without one
 * 
 * Validates: Requirements 8.1
 * 
 * This test verifies that:
 * 1. Password reset requests require a tenant slug
 * 2. Requests without a tenant slug are rejected
 * 3. Requests with invalid tenant slugs are rejected
 * 4. Tenant isolation is enforced throughout the password reset flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantRequirementIntegrationTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Property 23: Tenant slug requirement
     * 
     * For any password reset request, the system should require a tenant slug 
     * and reject requests without one.
     * 
     * **Feature: admin-forgot-password, Property 23: Tenant slug requirement**
     * **Validates: Requirements 8.1**
     */
    @Test
    public void passwordResetRequiresTenantSlug() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                StaffUser testStaff = createTestStaff(testTenant);
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Request password reset WITH tenant slug (should succeed)
                Optional<String> tokenWithTenant = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenWithTenant.isPresent(), 
                    "Password reset should succeed when tenant slug is provided");
                
                // Verify token was created
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Token should be created when tenant slug is provided");
                
                // Clear rate limit for next test
                passwordResetService.clearRateLimit(testStaff.getEmail(), "");
                
                // Request password reset WITHOUT tenant slug (empty string - should fail)
                Optional<String> tokenWithoutTenant = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    ""
                );
                assertFalse(tokenWithoutTenant.isPresent(), 
                    "Password reset should fail when tenant slug is empty");
                
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
     * Property 23 (variant): Invalid tenant slugs are rejected
     * 
     * For any password reset request with an invalid tenant slug, the system should 
     * reject the request.
     * 
     * **Feature: admin-forgot-password, Property 23: Tenant slug requirement**
     * **Validates: Requirements 8.1**
     */
    @Test
    public void invalidTenantSlugsAreRejected() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                StaffUser testStaff = createTestStaff(testTenant);
                
                // Generate a random invalid tenant slug
                String invalidTenantSlug = "invalid-tenant-" + UUID.randomUUID().toString();
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), invalidTenantSlug);
                
                // Request password reset with invalid tenant slug
                Optional<String> token = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    invalidTenantSlug
                );
                assertFalse(token.isPresent(), 
                    "Password reset should fail with invalid tenant slug");
                
                // Verify no token was created
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(0, tokens.size(), 
                    "No token should be created with invalid tenant slug");
                
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
     * Property 23 (variant): Cross-tenant password reset is prevented
     * 
     * For any password reset request, the system should verify that the email 
     * belongs to a staff member in the specified tenant.
     * 
     * **Feature: admin-forgot-password, Property 23: Tenant slug requirement**
     * **Validates: Requirements 8.1**
     */
    @Test
    public void crossTenantPasswordResetIsPrevented() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                
                // Create staff user in tenant1
                StaffUser staffInTenant1 = createTestStaff(tenant1);
                
                // Clear rate limit
                passwordResetService.clearRateLimit(staffInTenant1.getEmail(), tenant2.getSlug());
                
                // Attempt to reset password for tenant1 staff using tenant2 slug
                Optional<String> token = passwordResetService.requestPasswordResetForTesting(
                    staffInTenant1.getEmail(), 
                    tenant2.getSlug()
                );
                assertFalse(token.isPresent(), 
                    "Password reset should fail when email belongs to different tenant");
                
                // Verify no token was created
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staffInTenant1.getId());
                assertEquals(0, tokens.size(), 
                    "No token should be created for cross-tenant password reset attempt");
                
                // Verify correct tenant slug works
                passwordResetService.clearRateLimit(staffInTenant1.getEmail(), tenant1.getSlug());
                Optional<String> validToken = passwordResetService.requestPasswordResetForTesting(
                    staffInTenant1.getEmail(), 
                    tenant1.getSlug()
                );
                assertTrue(validToken.isPresent(), 
                    "Password reset should succeed with correct tenant slug");
                
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
     * Property 23 (variant): Token is associated with correct tenant
     * 
     * For any generated password reset token, it should be associated with the 
     * tenant specified in the request.
     * 
     * **Feature: admin-forgot-password, Property 23: Tenant slug requirement**
     * **Validates: Requirements 8.1**
     */
    @Test
    public void tokenIsAssociatedWithCorrectTenant() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                StaffUser testStaff = createTestStaff(testTenant);
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Request password reset
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                
                // Find the token entity
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Should have one token");
                PasswordResetTokenEntity tokenEntity = tokens.get(0);
                
                // Verify token is associated with correct tenant
                assertNotNull(tokenEntity.getTenant(), "Token should have tenant association");
                assertEquals(testTenant.getId(), tokenEntity.getTenant().getId(),
                    "Token should be associated with the correct tenant");
                assertEquals(testTenant.getSlug(), tokenEntity.getTenant().getSlug(),
                    "Token should be associated with tenant having correct slug");
                
                // Verify token validation returns correct tenant slug
                Optional<PasswordResetService.TokenValidationResult> validationResult = 
                    passwordResetService.validateResetTokenWithTenant(tokenOpt.get());
                assertTrue(validationResult.isPresent(), "Validation should return result");
                assertEquals(testTenant.getSlug(), validationResult.get().tenantSlug(),
                    "Validation should return correct tenant slug");
                
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
     * Property 23 (variant): Tenant isolation is maintained throughout password reset flow
     * 
     * For any password reset flow, tenant isolation should be maintained from 
     * request through validation to password reset.
     * 
     * **Feature: admin-forgot-password, Property 23: Tenant slug requirement**
     * **Validates: Requirements 8.1**
     */
    @Test
    public void tenantIsolationIsMaintainedThroughoutFlow() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                
                // Create staff users in both tenants with same email prefix
                String emailPrefix = "staff-" + UUID.randomUUID().toString().substring(0, 8);
                StaffUser staffInTenant1 = createTestStaffWithEmail(tenant1, emailPrefix + "@tenant1.com");
                StaffUser staffInTenant2 = createTestStaffWithEmail(tenant2, emailPrefix + "@tenant2.com");
                
                // Request password reset for tenant1 staff
                passwordResetService.clearRateLimit(staffInTenant1.getEmail(), tenant1.getSlug());
                Optional<String> token1 = passwordResetService.requestPasswordResetForTesting(
                    staffInTenant1.getEmail(), 
                    tenant1.getSlug()
                );
                assertTrue(token1.isPresent(), "Token should be generated for tenant1 staff");
                
                // Request password reset for tenant2 staff
                passwordResetService.clearRateLimit(staffInTenant2.getEmail(), tenant2.getSlug());
                Optional<String> token2 = passwordResetService.requestPasswordResetForTesting(
                    staffInTenant2.getEmail(), 
                    tenant2.getSlug()
                );
                assertTrue(token2.isPresent(), "Token should be generated for tenant2 staff");
                
                // Verify tokens are associated with correct tenants
                List<PasswordResetTokenEntity> tokens1 = 
                    tokenRepository.findByStaffIdAndUsedFalse(staffInTenant1.getId());
                assertEquals(1, tokens1.size(), "Should have one token for tenant1 staff");
                assertEquals(tenant1.getId(), tokens1.get(0).getTenant().getId(),
                    "Token1 should be associated with tenant1");
                
                List<PasswordResetTokenEntity> tokens2 = 
                    tokenRepository.findByStaffIdAndUsedFalse(staffInTenant2.getId());
                assertEquals(1, tokens2.size(), "Should have one token for tenant2 staff");
                assertEquals(tenant2.getId(), tokens2.get(0).getTenant().getId(),
                    "Token2 should be associated with tenant2");
                
                // Verify each token can only reset password for its own tenant's staff
                boolean reset1Success = passwordResetService.resetPassword(token1.get(), "NewPassword1!");
                assertTrue(reset1Success, "Token1 should reset password for tenant1 staff");
                
                boolean reset2Success = passwordResetService.resetPassword(token2.get(), "NewPassword2!");
                assertTrue(reset2Success, "Token2 should reset password for tenant2 staff");
                
                // Verify passwords were updated correctly
                StaffUser updatedStaff1 = staffUserRepository.findById(staffInTenant1.getId()).orElseThrow();
                assertTrue(passwordEncoder.matches("NewPassword1!", updatedStaff1.getPasswordHash()),
                    "Tenant1 staff should have new password");
                
                StaffUser updatedStaff2 = staffUserRepository.findById(staffInTenant2.getId()).orElseThrow();
                assertTrue(passwordEncoder.matches("NewPassword2!", updatedStaff2.getPasswordHash()),
                    "Tenant2 staff should have new password");
                
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
        return createTestStaffWithEmail(tenant, email);
    }

    private StaffUser createTestStaffWithEmail(TenantEntity tenant, String email) {
        String fullName = "Test Staff " + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = passwordEncoder.encode("OriginalPassword123!");
        
        StaffUser staff = new StaffUser(email, fullName, StaffRole.RECEPTIONIST, 
            passwordHash, StaffStatus.ACTIVE);
        staff.setTenant(tenant);
        
        return staffUserRepository.saveAndFlush(staff);
    }
}
