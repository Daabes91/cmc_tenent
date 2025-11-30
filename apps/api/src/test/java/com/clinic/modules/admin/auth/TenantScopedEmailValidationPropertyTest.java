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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for tenant-scoped email validation.
 * 
 * **Feature: admin-forgot-password, Property 24: Tenant-scoped email validation**
 * **Validates: Requirements 8.2**
 * 
 * This test verifies that:
 * 1. For any password reset request, the system verifies that the email belongs 
 *    to a staff member in the specified tenant before generating a token
 * 2. Emails from other tenants are rejected (no token generated)
 * 3. Non-existent emails are rejected (no token generated)
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantScopedEmailValidationPropertyTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Property 24: Tenant-scoped email validation
     * 
     * For any password reset request, the system should verify that the email 
     * belongs to a staff member in the specified tenant before generating a token.
     * 
     * **Feature: admin-forgot-password, Property 24: Tenant-scoped email validation**
     * **Validates: Requirements 8.2**
     */
    @Test
    public void passwordResetOnlyWorksForEmailsInSpecifiedTenant() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                
                // Create staff user in tenant1
                StaffUser staff1 = createTestStaff(tenant1);
                String email1 = staff1.getEmail();
                
                // Create staff user in tenant2 with same email (different tenant)
                StaffUser staff2 = createTestStaff(tenant2);
                String email2 = staff2.getEmail();
                
                // Clear rate limit for testing
                passwordResetService.clearRateLimit(email1, tenant1.getSlug());
                passwordResetService.clearRateLimit(email2, tenant2.getSlug());
                passwordResetService.clearRateLimit(email1, tenant2.getSlug());
                passwordResetService.clearRateLimit(email2, tenant1.getSlug());
                
                // Count tokens before requests
                long tokenCountBefore = tokenRepository.count();
                
                // Request password reset for staff1 in tenant1 (should succeed)
                Optional<String> token1 = passwordResetService.requestPasswordResetForTesting(
                    email1, tenant1.getSlug()
                );
                
                assertTrue(token1.isPresent(), 
                    "Password reset should succeed for email in correct tenant");
                
                // Verify token was created for staff1
                long tokenCountAfter1 = tokenRepository.count();
                assertEquals(tokenCountBefore + 1, tokenCountAfter1, 
                    "One token should be created for valid request");
                
                // Request password reset for staff1 email in tenant2 (should fail - wrong tenant)
                passwordResetService.clearRateLimit(email1, tenant2.getSlug());
                Optional<String> token2 = passwordResetService.requestPasswordResetForTesting(
                    email1, tenant2.getSlug()
                );
                
                assertFalse(token2.isPresent(), 
                    "Password reset should fail for email in wrong tenant");
                
                // Verify no additional token was created
                long tokenCountAfter2 = tokenRepository.count();
                assertEquals(tokenCountAfter1, tokenCountAfter2, 
                    "No token should be created for email in wrong tenant");
                
                // Request password reset for non-existent email in tenant1 (should fail)
                String nonExistentEmail = "nonexistent-" + UUID.randomUUID() + "@test.com";
                passwordResetService.clearRateLimit(nonExistentEmail, tenant1.getSlug());
                Optional<String> token3 = passwordResetService.requestPasswordResetForTesting(
                    nonExistentEmail, tenant1.getSlug()
                );
                
                assertFalse(token3.isPresent(), 
                    "Password reset should fail for non-existent email");
                
                // Verify no additional token was created
                long tokenCountAfter3 = tokenRepository.count();
                assertEquals(tokenCountAfter2, tokenCountAfter3, 
                    "No token should be created for non-existent email");
                
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
     * Property 24 (variant): Email validation is case-insensitive but tenant-scoped
     * 
     * For any password reset request, email matching should be case-insensitive
     * but still respect tenant boundaries.
     * 
     * **Feature: admin-forgot-password, Property 24: Tenant-scoped email validation**
     * **Validates: Requirements 8.2**
     */
    @Test
    public void emailValidationIsCaseInsensitiveButTenantScoped() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                
                // Create staff user in tenant1
                StaffUser staff1 = createTestStaff(tenant1);
                String email1 = staff1.getEmail().toLowerCase();
                
                // Clear rate limit for testing
                String emailUpperCase = email1.toUpperCase();
                String emailMixedCase = mixCase(email1);
                
                passwordResetService.clearRateLimit(email1, tenant1.getSlug());
                passwordResetService.clearRateLimit(emailUpperCase, tenant1.getSlug());
                passwordResetService.clearRateLimit(emailMixedCase, tenant1.getSlug());
                
                // Request with uppercase email in correct tenant (should succeed)
                Optional<String> token1 = passwordResetService.requestPasswordResetForTesting(
                    emailUpperCase, tenant1.getSlug()
                );
                
                assertTrue(token1.isPresent(), 
                    "Password reset should succeed with uppercase email in correct tenant");
                
                // Request with mixed case email in wrong tenant (should fail)
                passwordResetService.clearRateLimit(emailMixedCase, tenant2.getSlug());
                Optional<String> token2 = passwordResetService.requestPasswordResetForTesting(
                    emailMixedCase, tenant2.getSlug()
                );
                
                assertFalse(token2.isPresent(), 
                    "Password reset should fail even with case-insensitive match in wrong tenant");
                
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
     * Property 24 (variant): Tenant slug validation is case-insensitive
     * 
     * For any password reset request, tenant slug matching should be case-insensitive.
     * 
     * **Feature: admin-forgot-password, Property 24: Tenant-scoped email validation**
     * **Validates: Requirements 8.2**
     */
    @Test
    public void tenantSlugValidationIsCaseInsensitive() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create tenant
                TenantEntity tenant = createTestTenant();
                String tenantSlug = tenant.getSlug().toLowerCase();
                
                // Create staff user
                StaffUser staff = createTestStaff(tenant);
                String email = staff.getEmail();
                
                // Clear rate limit for testing
                String slugUpperCase = tenantSlug.toUpperCase();
                String slugMixedCase = mixCase(tenantSlug);
                
                passwordResetService.clearRateLimit(email, tenantSlug);
                passwordResetService.clearRateLimit(email, slugUpperCase);
                passwordResetService.clearRateLimit(email, slugMixedCase);
                
                // Request with uppercase tenant slug (should succeed)
                Optional<String> token1 = passwordResetService.requestPasswordResetForTesting(
                    email, slugUpperCase
                );
                
                assertTrue(token1.isPresent(), 
                    "Password reset should succeed with uppercase tenant slug");
                
                // Request with mixed case tenant slug (should succeed)
                passwordResetService.clearRateLimit(email, slugMixedCase);
                Optional<String> token2 = passwordResetService.requestPasswordResetForTesting(
                    email, slugMixedCase
                );
                
                assertTrue(token2.isPresent(), 
                    "Password reset should succeed with mixed case tenant slug");
                
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

    private String mixCase(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (i % 2 == 0) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
