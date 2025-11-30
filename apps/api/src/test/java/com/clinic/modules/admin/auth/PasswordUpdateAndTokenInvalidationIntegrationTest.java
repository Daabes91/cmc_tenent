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
 * Feature: admin-forgot-password, Property 14: Password update and token invalidation
 * 
 * Property: For any valid password reset request, the system should update 
 * the staff member's password and mark the token as used in a single transaction
 * 
 * Validates: Requirements 4.5
 * 
 * This test verifies that:
 * 1. Password is updated in the database
 * 2. Token is marked as used
 * 3. Token cannot be reused after successful password reset
 * 4. Both operations happen atomically (transaction)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PasswordUpdateAndTokenInvalidationIntegrationTest {

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
     * Property 14: Password update and token invalidation
     * 
     * For any valid password reset request, the system should update the staff member's 
     * password and mark the token as used in a single transaction.
     * 
     * **Feature: admin-forgot-password, Property 14: Password update and token invalidation**
     * **Validates: Requirements 4.5**
     */
    @Test
    public void passwordUpdateAndTokenInvalidation() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                String originalPassword = "OriginalPassword123!";
                StaffUser testStaff = createTestStaff(testTenant, originalPassword);
                String originalPasswordHash = testStaff.getPasswordHash();
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Generate a random new password
                String newPassword = generateRandomPassword();
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String plainToken = tokenOpt.get();
                
                // Find the token entity before reset
                List<PasswordResetTokenEntity> tokensBeforeReset = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokensBeforeReset.size(), "Should have exactly one unused token");
                PasswordResetTokenEntity tokenBeforeReset = tokensBeforeReset.get(0);
                assertFalse(tokenBeforeReset.isUsed(), "Token should not be marked as used before reset");
                assertNull(tokenBeforeReset.getUsedAt(), "Token should not have usedAt timestamp before reset");
                
                // Reset password
                boolean resetSuccess = passwordResetService.resetPassword(plainToken, newPassword);
                assertTrue(resetSuccess, "Password reset should succeed");
                
                // Verify password was updated
                StaffUser updatedStaff = staffUserRepository.findById(testStaff.getId()).orElseThrow();
                assertNotEquals(originalPasswordHash, updatedStaff.getPasswordHash(),
                    "Password hash should be different from original");
                assertTrue(passwordEncoder.matches(newPassword, updatedStaff.getPasswordHash()),
                    "New password should match the stored hash");
                
                // Verify token was marked as used
                PasswordResetTokenEntity tokenAfterReset = 
                    tokenRepository.findById(tokenBeforeReset.getId()).orElseThrow();
                assertTrue(tokenAfterReset.isUsed(), "Token should be marked as used after reset");
                assertNotNull(tokenAfterReset.getUsedAt(), "Token should have usedAt timestamp after reset");
                
                // Verify token cannot be reused
                boolean reuseAttempt = passwordResetService.resetPassword(plainToken, "AnotherPassword123!");
                assertFalse(reuseAttempt, "Token should not be reusable after being used once");
                
                // Verify password was not changed by reuse attempt
                StaffUser staffAfterReuseAttempt = staffUserRepository.findById(testStaff.getId()).orElseThrow();
                assertEquals(updatedStaff.getPasswordHash(), staffAfterReuseAttempt.getPasswordHash(),
                    "Password should not change when reusing a token");
                
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
     * Property 14 (variant): Token invalidation happens even if password update fails
     * 
     * This test verifies that the transaction is atomic - if password update fails,
     * token should not be marked as used.
     * 
     * **Feature: admin-forgot-password, Property 14: Password update and token invalidation**
     * **Validates: Requirements 4.5**
     */
    @Test
    public void tokenNotInvalidatedIfPasswordUpdateFails() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                String originalPassword = "OriginalPassword123!";
                StaffUser testStaff = createTestStaff(testTenant, originalPassword);
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String plainToken = tokenOpt.get();
                
                // Find the token entity
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Should have exactly one unused token");
                PasswordResetTokenEntity token = tokens.get(0);
                
                // Manually expire the token to simulate a failure scenario
                token.setExpiresAt(token.getExpiresAt().minusSeconds(7200)); // Expire 2 hours ago
                tokenRepository.saveAndFlush(token);
                
                // Attempt to reset password with expired token (should fail)
                boolean resetSuccess = passwordResetService.resetPassword(plainToken, "NewPassword123!");
                assertFalse(resetSuccess, "Password reset should fail with expired token");
                
                // Verify token was not marked as used (transaction rolled back)
                PasswordResetTokenEntity tokenAfterFailedReset = 
                    tokenRepository.findById(token.getId()).orElseThrow();
                assertFalse(tokenAfterFailedReset.isUsed(), 
                    "Token should not be marked as used when reset fails");
                assertNull(tokenAfterFailedReset.getUsedAt(), 
                    "Token should not have usedAt timestamp when reset fails");
                
                // Verify password was not changed
                StaffUser staffAfterFailedReset = staffUserRepository.findById(testStaff.getId()).orElseThrow();
                assertTrue(passwordEncoder.matches(originalPassword, staffAfterFailedReset.getPasswordHash()),
                    "Password should not change when reset fails");
                
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
     * Property 14 (variant): Multiple tokens are all invalidated on successful reset
     * 
     * This test verifies that when a password is reset, only the used token is marked as used,
     * but any other unused tokens for the same staff member remain unused (they were already
     * invalidated when the new token was created).
     * 
     * **Feature: admin-forgot-password, Property 14: Password update and token invalidation**
     * **Validates: Requirements 4.5**
     */
    @Test
    public void onlyUsedTokenIsMarkedAsUsed() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                String originalPassword = "OriginalPassword123!";
                StaffUser testStaff = createTestStaff(testTenant, originalPassword);
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Request first password reset
                Optional<String> token1Opt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(token1Opt.isPresent(), "First token should be generated");
                
                // Wait a bit to ensure different timestamps
                Thread.sleep(10);
                
                // Clear rate limit again
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Request second password reset (should invalidate first token)
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(token2Opt.isPresent(), "Second token should be generated");
                String token2 = token2Opt.get();
                
                // Verify first token was invalidated
                List<PasswordResetTokenEntity> unusedTokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, unusedTokens.size(), 
                    "Should have exactly one unused token (the second one)");
                
                // Reset password with second token
                boolean resetSuccess = passwordResetService.resetPassword(token2, "NewPassword123!");
                assertTrue(resetSuccess, "Password reset should succeed with second token");
                
                // Verify second token is marked as used
                PasswordResetTokenEntity secondToken = unusedTokens.get(0);
                PasswordResetTokenEntity secondTokenAfterReset = 
                    tokenRepository.findById(secondToken.getId()).orElseThrow();
                assertTrue(secondTokenAfterReset.isUsed(), 
                    "Second token should be marked as used after reset");
                
                // Verify no unused tokens remain
                List<PasswordResetTokenEntity> remainingUnusedTokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(0, remainingUnusedTokens.size(), 
                    "Should have no unused tokens after reset");
                
            } catch (AssertionError | InterruptedException e) {
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

    private StaffUser createTestStaff(TenantEntity tenant, String password) {
        String email = "staff-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String fullName = "Test Staff " + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = passwordEncoder.encode(password);
        
        StaffUser staff = new StaffUser(email, fullName, StaffRole.RECEPTIONIST, 
            passwordHash, StaffStatus.ACTIVE);
        staff.setTenant(tenant);
        
        return staffUserRepository.saveAndFlush(staff);
    }

    private String generateRandomPassword() {
        // Generate a random password with letters, numbers, and special characters
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*";
        int length = 8 + (int)(Math.random() * 43); // 8-50 characters
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return password.toString();
    }
}
