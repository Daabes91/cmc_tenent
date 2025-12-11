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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: admin-forgot-password, Property 9: Token validation on page load
 * 
 * Property: For any password reset page load with a token, the system should 
 * validate the token with the backend API before allowing password entry
 * 
 * Validates: Requirements 3.3
 * 
 * This test verifies that:
 * 1. Valid tokens are correctly validated
 * 2. Invalid tokens are rejected
 * 3. Expired tokens are rejected
 * 4. Used tokens are rejected
 * 5. Token validation returns appropriate results
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TokenValidationOnPageLoadIntegrationTest {

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
     * Property 9: Token validation on page load
     * 
     * For any password reset page load with a token, the system should validate 
     * the token with the backend API before allowing password entry.
     * 
     * **Feature: admin-forgot-password, Property 9: Token validation on page load**
     * **Validates: Requirements 3.3**
     */
    @Test
    public void validTokensAreAcceptedOnPageLoad() {
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
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String token = tokenOpt.get();
                
                // Simulate page load: validate token
                boolean isValid = passwordResetService.validateResetToken(token);
                assertTrue(isValid, "Valid token should be accepted on page load");
                
                // Verify validation returns tenant information
                Optional<PasswordResetService.TokenValidationResult> validationResult = 
                    passwordResetService.validateResetTokenWithTenant(token);
                assertTrue(validationResult.isPresent(), 
                    "Validation should return result for valid token");
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
     * Property 9 (variant): Invalid tokens are rejected on page load
     * 
     * For any password reset page load with an invalid token, the system should 
     * reject the token and not allow password entry.
     * 
     * **Feature: admin-forgot-password, Property 9: Token validation on page load**
     * **Validates: Requirements 3.3**
     */
    @Test
    public void invalidTokensAreRejectedOnPageLoad() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Generate a random invalid token
                String invalidToken = "invalid-token-" + UUID.randomUUID().toString();
                
                // Simulate page load: validate token
                boolean isValid = passwordResetService.validateResetToken(invalidToken);
                assertFalse(isValid, "Invalid token should be rejected on page load");
                
                // Verify validation returns empty result
                Optional<PasswordResetService.TokenValidationResult> validationResult = 
                    passwordResetService.validateResetTokenWithTenant(invalidToken);
                assertFalse(validationResult.isPresent(), 
                    "Validation should return empty result for invalid token");
                
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
     * Property 9 (variant): Expired tokens are rejected on page load
     * 
     * For any password reset page load with an expired token, the system should 
     * reject the token and not allow password entry.
     * 
     * **Feature: admin-forgot-password, Property 9: Token validation on page load**
     * **Validates: Requirements 3.3**
     */
    @Test
    public void expiredTokensAreRejectedOnPageLoad() {
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
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String token = tokenOpt.get();
                
                // Manually expire the token
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Should have one token");
                PasswordResetTokenEntity tokenEntity = tokens.get(0);
                tokenEntity.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
                tokenRepository.saveAndFlush(tokenEntity);
                
                // Simulate page load: validate expired token
                boolean isValid = passwordResetService.validateResetToken(token);
                assertFalse(isValid, "Expired token should be rejected on page load");
                
                // Verify validation returns empty result
                Optional<PasswordResetService.TokenValidationResult> validationResult = 
                    passwordResetService.validateResetTokenWithTenant(token);
                assertFalse(validationResult.isPresent(), 
                    "Validation should return empty result for expired token");
                
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
     * Property 9 (variant): Used tokens are rejected on page load
     * 
     * For any password reset page load with a used token, the system should 
     * reject the token and not allow password entry.
     * 
     * **Feature: admin-forgot-password, Property 9: Token validation on page load**
     * **Validates: Requirements 3.3**
     */
    @Test
    public void usedTokensAreRejectedOnPageLoad() {
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
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String token = tokenOpt.get();
                
                // Use the token to reset password
                boolean resetSuccess = passwordResetService.resetPassword(token, "NewPassword123!");
                assertTrue(resetSuccess, "Password reset should succeed");
                
                // Simulate page load with used token: validate token
                boolean isValid = passwordResetService.validateResetToken(token);
                assertFalse(isValid, "Used token should be rejected on page load");
                
                // Verify validation returns empty result
                Optional<PasswordResetService.TokenValidationResult> validationResult = 
                    passwordResetService.validateResetTokenWithTenant(token);
                assertFalse(validationResult.isPresent(), 
                    "Validation should return empty result for used token");
                
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
     * Property 9 (variant): Token validation is idempotent
     * 
     * For any valid token, validating it multiple times should always return the same result
     * (validation should not modify the token state).
     * 
     * **Feature: admin-forgot-password, Property 9: Token validation on page load**
     * **Validates: Requirements 3.3**
     */
    @Test
    public void tokenValidationIsIdempotent() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                StaffUser testStaff = createTestStaff(testTenant);
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String token = tokenOpt.get();
                
                // Validate token multiple times
                boolean firstValidation = passwordResetService.validateResetToken(token);
                boolean secondValidation = passwordResetService.validateResetToken(token);
                boolean thirdValidation = passwordResetService.validateResetToken(token);
                
                // All validations should return the same result
                assertTrue(firstValidation, "First validation should succeed");
                assertTrue(secondValidation, "Second validation should succeed");
                assertTrue(thirdValidation, "Third validation should succeed");
                
                // Verify token is still unused
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Token should still be unused after multiple validations");
                
                // Verify token can still be used to reset password
                boolean resetSuccess = passwordResetService.resetPassword(token, "NewPassword123!");
                assertTrue(resetSuccess, "Token should still be usable after multiple validations");
                
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
        String passwordHash = passwordEncoder.encode("OriginalPassword123!");
        
        StaffUser staff = new StaffUser(email, fullName, StaffRole.RECEPTIONIST, 
            passwordHash, StaffStatus.ACTIVE);
        staff.setTenant(tenant);
        
        return staffUserRepository.saveAndFlush(staff);
    }
}
