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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for password reset token generation with correct expiration.
 * 
 * **Feature: admin-forgot-password, Property 3: Token generation with correct expiration**
 * **Validates: Requirements 1.4**
 * 
 * This test verifies that:
 * 1. For any valid email and tenant combination, when a reset token is generated,
 *    it has an expiration time exactly 1 hour from creation
 * 2. Token expiration is calculated correctly
 * 3. Expired tokens are properly identified
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TokenExpirationPropertyTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private static final long EXPECTED_EXPIRATION_HOURS = 1;
    private static final long TOLERANCE_SECONDS = 5; // Allow 5 seconds tolerance for test execution time

    /**
     * Property 3: Token generation with correct expiration
     * 
     * For any valid email and tenant combination, when a reset token is generated,
     * it should have an expiration time exactly 1 hour from creation.
     * 
     * **Feature: admin-forgot-password, Property 3: Token generation with correct expiration**
     * **Validates: Requirements 1.4**
     */
    @Test
    public void tokenHasCorrectExpirationTime() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Record time before request
                Instant beforeRequest = Instant.now();
                
                // Request password reset
                Optional<String> plainTokenOpt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                
                // Record time after request
                Instant afterRequest = Instant.now();
                
                assertTrue(plainTokenOpt.isPresent(), 
                    "Password reset should generate a token");
                
                // Find the stored token
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                
                assertEquals(1, tokens.size(), 
                    "Should have exactly one unused token for staff");
                
                PasswordResetTokenEntity token = tokens.get(0);
                
                // Verify expiration time is approximately 1 hour from creation
                Instant createdAt = token.getCreatedAt();
                Instant expiresAt = token.getExpiresAt();
                
                assertNotNull(createdAt, "Token should have creation timestamp");
                assertNotNull(expiresAt, "Token should have expiration timestamp");
                
                // Calculate expected expiration
                Instant expectedExpiration = createdAt.plus(EXPECTED_EXPIRATION_HOURS, ChronoUnit.HOURS);
                
                // Verify expiration is exactly 1 hour from creation (within tolerance)
                long secondsDifference = Math.abs(Duration.between(expiresAt, expectedExpiration).getSeconds());
                assertTrue(secondsDifference <= TOLERANCE_SECONDS, 
                    String.format("Token expiration should be exactly %d hour(s) from creation. " +
                        "Expected: %s, Actual: %s, Difference: %d seconds",
                        EXPECTED_EXPIRATION_HOURS, expectedExpiration, expiresAt, secondsDifference));
                
                // Verify creation time is within the request time window
                assertFalse(createdAt.isBefore(beforeRequest), 
                    "Token creation time should not be before request started");
                assertFalse(createdAt.isAfter(afterRequest.plusSeconds(1)), 
                    "Token creation time should not be after request completed");
                
                // Verify expiration is in the future
                assertTrue(expiresAt.isAfter(Instant.now()), 
                    "Token expiration should be in the future");
                
                // Verify token is not expired
                assertFalse(token.isExpired(), 
                    "Newly created token should not be expired");
                
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
     * Property 3 (variant): Expired tokens are correctly identified
     * 
     * For any token with an expiration time in the past, the isExpired() method
     * should return true.
     * 
     * **Feature: admin-forgot-password, Property 3: Token generation with correct expiration**
     * **Validates: Requirements 1.4**
     */
    @Test
    public void expiredTokensAreCorrectlyIdentified() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Create a token with expiration in the past
                String tokenHash = "$2a$10$" + UUID.randomUUID().toString().replace("-", "") + 
                                  UUID.randomUUID().toString().replace("-", "").substring(0, 28);
                
                // Set expiration to random time in the past (1 second to 24 hours ago)
                int secondsAgo = 1 + (int)(Math.random() * 86400);
                Instant expiresAt = Instant.now().minus(secondsAgo, ChronoUnit.SECONDS);
                
                PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                    tokenHash, staff, tenant, expiresAt
                );
                
                PasswordResetTokenEntity savedToken = tokenRepository.saveAndFlush(token);
                
                // Verify token is identified as expired
                assertTrue(savedToken.isExpired(), 
                    String.format("Token with expiration %d seconds ago should be expired", secondsAgo));
                
                // Verify expiration time is in the past
                assertTrue(savedToken.getExpiresAt().isBefore(Instant.now()), 
                    "Expired token's expiration time should be in the past");
                
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
     * Property 3 (variant): Non-expired tokens are correctly identified
     * 
     * For any token with an expiration time in the future, the isExpired() method
     * should return false.
     * 
     * **Feature: admin-forgot-password, Property 3: Token generation with correct expiration**
     * **Validates: Requirements 1.4**
     */
    @Test
    public void nonExpiredTokensAreCorrectlyIdentified() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Create a token with expiration in the future
                String tokenHash = "$2a$10$" + UUID.randomUUID().toString().replace("-", "") + 
                                  UUID.randomUUID().toString().replace("-", "").substring(0, 28);
                
                // Set expiration to random time in the future (1 second to 24 hours from now)
                int secondsFromNow = 1 + (int)(Math.random() * 86400);
                Instant expiresAt = Instant.now().plus(secondsFromNow, ChronoUnit.SECONDS);
                
                PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                    tokenHash, staff, tenant, expiresAt
                );
                
                PasswordResetTokenEntity savedToken = tokenRepository.saveAndFlush(token);
                
                // Verify token is not identified as expired
                assertFalse(savedToken.isExpired(), 
                    String.format("Token with expiration %d seconds from now should not be expired", secondsFromNow));
                
                // Verify expiration time is in the future
                assertTrue(savedToken.getExpiresAt().isAfter(Instant.now()), 
                    "Non-expired token's expiration time should be in the future");
                
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
     * Property 3 (variant): Token validation respects expiration
     * 
     * For any expired token, validation should fail even if the token hash is correct.
     * 
     * **Feature: admin-forgot-password, Property 3: Token generation with correct expiration**
     * **Validates: Requirements 1.4**
     */
    @Test
    public void tokenValidationRespectsExpiration() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Request password reset
                Optional<String> plainTokenOpt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(plainTokenOpt.isPresent(), "Token should be generated");
                String plainToken = plainTokenOpt.get();
                
                // Verify token is valid initially
                assertTrue(passwordResetService.validateResetToken(plainToken), 
                    "Token should be valid initially");
                
                // Manually expire the token by updating its expiration time
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokens.size(), "Should have one token");
                
                PasswordResetTokenEntity token = tokens.get(0);
                token.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
                tokenRepository.saveAndFlush(token);
                
                // Verify token is now expired
                PasswordResetTokenEntity expiredToken = tokenRepository.findById(token.getId()).orElseThrow();
                assertTrue(expiredToken.isExpired(), "Token should be expired after manual update");
                
                // Verify validation now fails
                assertFalse(passwordResetService.validateResetToken(plainToken), 
                    "Validation should fail for expired token");
                
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
