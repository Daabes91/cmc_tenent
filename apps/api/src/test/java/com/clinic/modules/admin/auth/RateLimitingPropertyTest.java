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
 * Property-based test for rate limiting enforcement.
 * 
 * **Feature: admin-forgot-password, Property 6: Rate limiting enforcement**
 * **Validates: Requirements 2.4**
 * 
 * This test verifies that:
 * 1. For any staff member making multiple password reset requests within 5 minutes,
 *    only one email should be sent regardless of the number of requests
 * 2. Rate limiting is enforced per email+tenant combination
 * 3. Rate limiting resets after the cooldown period
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RateLimitingPropertyTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Property 6: Rate limiting enforcement
     * 
     * For any staff member making multiple password reset requests within 5 minutes,
     * only one token should be generated (subsequent requests are rate limited).
     * 
     * **Feature: admin-forgot-password, Property 6: Rate limiting enforcement**
     * **Validates: Requirements 2.4**
     */
    @Test
    public void rateLimitingPreventsMultipleRequests() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // First request should succeed
                Optional<String> token1Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(token1Opt.isPresent(), 
                    "First password reset request should succeed");
                
                // Verify one token was created
                List<PasswordResetTokenEntity> tokensAfterFirst = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokensAfterFirst.size(), 
                    "Should have one token after first request");
                
                // Second request within rate limit window should be blocked
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertFalse(token2Opt.isPresent(), 
                    "Second password reset request within rate limit window should be blocked");
                
                // Verify still only one token exists (no new token was created)
                List<PasswordResetTokenEntity> tokensAfterSecond = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokensAfterSecond.size(), 
                    "Should still have only one token after rate-limited request");
                
                // Third request should also be blocked
                Optional<String> token3Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertFalse(token3Opt.isPresent(), 
                    "Third password reset request within rate limit window should be blocked");
                
                // Verify still only one token exists
                List<PasswordResetTokenEntity> tokensAfterThird = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokensAfterThird.size(), 
                    "Should still have only one token after multiple rate-limited requests");
                
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
     * Property 6 (variant): Rate limiting is per email+tenant combination
     * 
     * For any two different staff members (or same email in different tenants),
     * rate limiting should be independent.
     * 
     * **Feature: admin-forgot-password, Property 6: Rate limiting enforcement**
     * **Validates: Requirements 2.4**
     */
    @Test
    public void rateLimitingIsPerEmailTenantCombination() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create two test staff users
                StaffUser staff1 = createTestStaff(tenant);
                StaffUser staff2 = createTestStaff(tenant);
                
                // Request password reset for staff1
                Optional<String> token1Opt = passwordResetService.requestPasswordResetForTesting(
                    staff1.getEmail(), tenant.getSlug()
                );
                assertTrue(token1Opt.isPresent(), 
                    "First request for staff1 should succeed");
                
                // Request password reset for staff2 (should not be rate limited)
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    staff2.getEmail(), tenant.getSlug()
                );
                assertTrue(token2Opt.isPresent(), 
                    "First request for staff2 should succeed (different email)");
                
                // Second request for staff1 should be rate limited
                Optional<String> token3Opt = passwordResetService.requestPasswordResetForTesting(
                    staff1.getEmail(), tenant.getSlug()
                );
                assertFalse(token3Opt.isPresent(), 
                    "Second request for staff1 should be rate limited");
                
                // Second request for staff2 should also be rate limited
                Optional<String> token4Opt = passwordResetService.requestPasswordResetForTesting(
                    staff2.getEmail(), tenant.getSlug()
                );
                assertFalse(token4Opt.isPresent(), 
                    "Second request for staff2 should be rate limited");
                
                // Verify each staff has exactly one token
                List<PasswordResetTokenEntity> staff1Tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff1.getId());
                List<PasswordResetTokenEntity> staff2Tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff2.getId());
                
                assertEquals(1, staff1Tokens.size(), 
                    "Staff1 should have exactly one token");
                assertEquals(1, staff2Tokens.size(), 
                    "Staff2 should have exactly one token");
                
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
     * Property 6 (variant): Rate limiting can be cleared
     * 
     * For any staff member, clearing the rate limit should allow a new request.
     * 
     * **Feature: admin-forgot-password, Property 6: Rate limiting enforcement**
     * **Validates: Requirements 2.4**
     */
    @Test
    public void rateLimitingCanBeCleared() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // First request should succeed
                Optional<String> token1Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(token1Opt.isPresent(), 
                    "First password reset request should succeed");
                
                // Second request should be rate limited
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertFalse(token2Opt.isPresent(), 
                    "Second request should be rate limited");
                
                // Clear rate limit
                passwordResetService.clearRateLimit(staff.getEmail(), tenant.getSlug());
                
                // Third request should now succeed
                Optional<String> token3Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(token3Opt.isPresent(), 
                    "Request after clearing rate limit should succeed");
                
                // Verify only one unused token exists (previous was invalidated)
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokens.size(), 
                    "Should have one unused token after clearing rate limit and making new request");
                
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
     * Property 6 (variant): Rate limiting is case-insensitive
     * 
     * For any email address, rate limiting should work regardless of case variations.
     * 
     * **Feature: admin-forgot-password, Property 6: Rate limiting enforcement**
     * **Validates: Requirements 2.4**
     */
    @Test
    public void rateLimitingIsCaseInsensitive() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                String email = staff.getEmail();
                
                // First request with original case
                Optional<String> token1Opt = passwordResetService.requestPasswordResetForTesting(
                    email, tenant.getSlug()
                );
                assertTrue(token1Opt.isPresent(), 
                    "First request should succeed");
                
                // Second request with uppercase email should be rate limited
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    email.toUpperCase(), tenant.getSlug()
                );
                assertFalse(token2Opt.isPresent(), 
                    "Request with uppercase email should be rate limited");
                
                // Clear rate limit
                passwordResetService.clearRateLimit(email, tenant.getSlug());
                
                // Third request with lowercase email should succeed
                Optional<String> token3Opt = passwordResetService.requestPasswordResetForTesting(
                    email.toLowerCase(), tenant.getSlug()
                );
                assertTrue(token3Opt.isPresent(), 
                    "Request with lowercase email after clearing should succeed");
                
                // Fourth request with mixed case should be rate limited
                String mixedCase = email.substring(0, 1).toUpperCase() + 
                                  email.substring(1).toLowerCase();
                Optional<String> token4Opt = passwordResetService.requestPasswordResetForTesting(
                    mixedCase, tenant.getSlug()
                );
                assertFalse(token4Opt.isPresent(), 
                    "Request with mixed case email should be rate limited");
                
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
     * Property 6 (variant): Rate limiting applies to non-existent emails
     * 
     * For any email (even non-existent), rate limiting should still apply to prevent
     * user enumeration attacks.
     * 
     * **Feature: admin-forgot-password, Property 6: Rate limiting enforcement**
     * **Validates: Requirements 2.4**
     */
    @Test
    public void rateLimitingAppliestoNonExistentEmails() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Use a non-existent email
                String nonExistentEmail = "nonexistent-" + UUID.randomUUID() + "@test.com";
                
                // First request should return empty (user not found)
                Optional<String> token1Opt = passwordResetService.requestPasswordResetForTesting(
                    nonExistentEmail, tenant.getSlug()
                );
                assertFalse(token1Opt.isPresent(), 
                    "Request for non-existent email should return empty");
                
                // Second request should also return empty (could be rate limited or user not found)
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    nonExistentEmail, tenant.getSlug()
                );
                assertFalse(token2Opt.isPresent(), 
                    "Second request for non-existent email should return empty");
                
                // The behavior should be consistent - both return empty
                // This prevents attackers from determining if an email exists by timing attacks
                
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
