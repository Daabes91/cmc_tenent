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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: admin-forgot-password, Property 7: Reset link expiration
 * 
 * Property: For any generated reset link, it should be valid for exactly 1 hour from creation time
 * 
 * Validates: Requirements 3.1
 * 
 * This test verifies that:
 * 1. Reset links expire after exactly 1 hour
 * 2. Expired links cannot be used to reset passwords
 * 3. Links are valid before expiration
 * 4. Expiration is enforced consistently
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ResetLinkExpirationIntegrationTest {

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

    private static final long EXPECTED_EXPIRATION_HOURS = 1;
    private static final long TOLERANCE_SECONDS = 5;

    /**
     * Property 7: Reset link expiration
     * 
     * For any generated reset link, it should be valid for exactly 1 hour from creation time.
     * 
     * **Feature: admin-forgot-password, Property 7: Reset link expiration**
     * **Validates: Requirements 3.1**
     */
    @Test
    public void resetLinkExpiresAfterOneHour() {
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
                
                // Record time before request
                Instant beforeRequest = Instant.now();
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String token = tokenOpt.get();
                
                // Record time after request
                Instant afterRequest = Instant.now();
                
                // Find the token entity
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Should have one token");
                PasswordResetTokenEntity tokenEntity = tokens.get(0);
                
                // Verify expiration is exactly 1 hour from creation
                Instant createdAt = tokenEntity.getCreatedAt();
                Instant expiresAt = tokenEntity.getExpiresAt();
                
                Instant expectedExpiration = createdAt.plus(EXPECTED_EXPIRATION_HOURS, ChronoUnit.HOURS);
                long secondsDifference = Math.abs(Duration.between(expiresAt, expectedExpiration).getSeconds());
                
                assertTrue(secondsDifference <= TOLERANCE_SECONDS,
                    String.format("Reset link should expire exactly %d hour(s) from creation. " +
                        "Expected: %s, Actual: %s, Difference: %d seconds",
                        EXPECTED_EXPIRATION_HOURS, expectedExpiration, expiresAt, secondsDifference));
                
                // Verify creation time is within request window
                assertFalse(createdAt.isBefore(beforeRequest),
                    "Creation time should not be before request started");
                assertFalse(createdAt.isAfter(afterRequest.plusSeconds(1)),
                    "Creation time should not be after request completed");
                
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
     * Property 7 (variant): Expired reset links cannot be used
     * 
     * For any reset link that has expired, attempting to use it should fail.
     * 
     * **Feature: admin-forgot-password, Property 7: Reset link expiration**
     * **Validates: Requirements 3.1**
     */
    @Test
    public void expiredResetLinksCannotBeUsed() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                StaffUser testStaff = createTestStaff(testTenant);
                String originalPasswordHash = testStaff.getPasswordHash();
                
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
                
                // Set expiration to random time in the past (1 second to 24 hours ago)
                int secondsAgo = 1 + (int)(Math.random() * 86400);
                tokenEntity.setExpiresAt(Instant.now().minus(secondsAgo, ChronoUnit.SECONDS));
                tokenRepository.saveAndFlush(tokenEntity);
                
                // Verify token is expired
                PasswordResetTokenEntity expiredToken = tokenRepository.findById(tokenEntity.getId()).orElseThrow();
                assertTrue(expiredToken.isExpired(), "Token should be expired");
                
                // Attempt to use expired token
                boolean resetSuccess = passwordResetService.resetPassword(token, "NewPassword123!");
                assertFalse(resetSuccess, "Password reset should fail with expired token");
                
                // Verify password was not changed
                StaffUser staffAfterAttempt = staffUserRepository.findById(testStaff.getId()).orElseThrow();
                assertEquals(originalPasswordHash, staffAfterAttempt.getPasswordHash(),
                    "Password should not change when using expired token");
                
                // Verify token validation fails
                boolean isValid = passwordResetService.validateResetToken(token);
                assertFalse(isValid, "Expired token should not validate");
                
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
     * Property 7 (variant): Non-expired reset links can be used
     * 
     * For any reset link that has not expired, it should be usable to reset the password.
     * 
     * **Feature: admin-forgot-password, Property 7: Reset link expiration**
     * **Validates: Requirements 3.1**
     */
    @Test
    public void nonExpiredResetLinksCanBeUsed() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user
                StaffUser testStaff = createTestStaff(testTenant);
                String originalPasswordHash = testStaff.getPasswordHash();
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String token = tokenOpt.get();
                
                // Verify token is not expired
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Should have one token");
                PasswordResetTokenEntity tokenEntity = tokens.get(0);
                assertFalse(tokenEntity.isExpired(), "Token should not be expired");
                
                // Verify token validation succeeds
                boolean isValid = passwordResetService.validateResetToken(token);
                assertTrue(isValid, "Non-expired token should validate");
                
                // Use token to reset password
                String newPassword = "NewPassword123!";
                boolean resetSuccess = passwordResetService.resetPassword(token, newPassword);
                assertTrue(resetSuccess, "Password reset should succeed with non-expired token");
                
                // Verify password was changed
                StaffUser staffAfterReset = staffUserRepository.findById(testStaff.getId()).orElseThrow();
                assertNotEquals(originalPasswordHash, staffAfterReset.getPasswordHash(),
                    "Password should change when using non-expired token");
                assertTrue(passwordEncoder.matches(newPassword, staffAfterReset.getPasswordHash()),
                    "New password should match stored hash");
                
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
     * Property 7 (variant): Reset link expiration is enforced at validation time
     * 
     * For any reset link, if it expires between creation and validation, 
     * the validation should correctly identify it as expired.
     * 
     * **Feature: admin-forgot-password, Property 7: Reset link expiration**
     * **Validates: Requirements 3.1**
     */
    @Test
    public void expirationIsEnforcedAtValidationTime() {
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
                
                // Verify token is valid initially
                boolean initialValidation = passwordResetService.validateResetToken(token);
                assertTrue(initialValidation, "Token should be valid initially");
                
                // Manually expire the token
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(testStaff.getId());
                assertEquals(1, tokens.size(), "Should have one token");
                PasswordResetTokenEntity tokenEntity = tokens.get(0);
                tokenEntity.setExpiresAt(Instant.now().minus(1, ChronoUnit.SECONDS));
                tokenRepository.saveAndFlush(tokenEntity);
                
                // Verify token is now invalid
                boolean laterValidation = passwordResetService.validateResetToken(token);
                assertFalse(laterValidation, "Token should be invalid after expiration");
                
                // Verify the token entity is marked as expired
                PasswordResetTokenEntity expiredToken = tokenRepository.findById(tokenEntity.getId()).orElseThrow();
                assertTrue(expiredToken.isExpired(), "Token entity should be marked as expired");
                
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
