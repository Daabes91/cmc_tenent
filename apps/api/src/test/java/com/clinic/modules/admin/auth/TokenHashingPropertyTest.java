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
 * Property-based test for password reset token hashing before storage.
 * 
 * **Feature: admin-forgot-password, Property 19: Token hashing before storage**
 * **Validates: Requirements 6.2**
 * 
 * This test verifies that:
 * 1. For any password reset token stored in the database, the token value is hashed using BCrypt
 * 2. Plain tokens are never stored in the database
 * 3. Hashed tokens can be verified against plain tokens using BCrypt
 * 4. Each token generates a unique hash (due to BCrypt salt)
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TokenHashingPropertyTest {

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
     * Property 19: Token hashing before storage
     * 
     * For any password reset token stored in the database, the token value should be
     * hashed using BCrypt, never stored in plaintext.
     * 
     * **Feature: admin-forgot-password, Property 19: Token hashing before storage**
     * **Validates: Requirements 6.2**
     */
    @Test
    public void tokenIsHashedBeforeStorage() {
        // Run property test with 100 iterations
        int tries = 100;
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
                
                assertTrue(plainTokenOpt.isPresent(), 
                    "Password reset should generate a token");
                
                String plainToken = plainTokenOpt.get();
                
                // Verify plain token is not empty
                assertNotNull(plainToken, "Plain token should not be null");
                assertFalse(plainToken.isEmpty(), "Plain token should not be empty");
                
                // Find the stored token
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                
                assertEquals(1, tokens.size(), 
                    "Should have exactly one unused token for staff");
                
                PasswordResetTokenEntity storedToken = tokens.get(0);
                String storedHash = storedToken.getTokenHash();
                
                // Verify the stored value is NOT the plain token
                assertNotEquals(plainToken, storedHash, 
                    "Stored token should be hashed, not plaintext");
                
                // Verify the stored value looks like a BCrypt hash
                assertTrue(storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$"), 
                    "Stored token should be a BCrypt hash (starts with $2a$, $2b$, or $2y$)");
                assertTrue(storedHash.length() == 60, 
                    "BCrypt hash should be 60 characters long");
                
                // Verify the hash can be verified against the plain token
                assertTrue(passwordEncoder.matches(plainToken, storedHash), 
                    "Stored hash should match the plain token when verified with BCrypt");
                
                // Verify a different token doesn't match
                String differentToken = "different-token-" + UUID.randomUUID();
                assertFalse(passwordEncoder.matches(differentToken, storedHash), 
                    "Different token should not match the stored hash");
                
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
     * Property 19 (variant): Each token generates a unique hash
     * 
     * For any two password reset requests, even for the same staff member,
     * the generated hashes should be different due to BCrypt's salt.
     * 
     * **Feature: admin-forgot-password, Property 19: Token hashing before storage**
     * **Validates: Requirements 6.2**
     */
    @Test
    public void eachTokenGeneratesUniqueHash() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Request first password reset
                Optional<String> token1Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(token1Opt.isPresent(), "First token should be generated");
                String plainToken1 = token1Opt.get();
                
                // Get the first stored hash
                List<PasswordResetTokenEntity> tokens1 = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokens1.size(), "Should have one token after first request");
                String hash1 = tokens1.get(0).getTokenHash();
                
                // Clear rate limit to allow second request
                passwordResetService.clearRateLimit(staff.getEmail(), tenant.getSlug());
                
                // Request second password reset
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(token2Opt.isPresent(), "Second token should be generated");
                String plainToken2 = token2Opt.get();
                
                // Get the second stored hash (first should be invalidated)
                List<PasswordResetTokenEntity> tokens2 = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokens2.size(), "Should have one unused token after second request");
                String hash2 = tokens2.get(0).getTokenHash();
                
                // Verify plain tokens are different
                assertNotEquals(plainToken1, plainToken2, 
                    "Each request should generate a different plain token");
                
                // Verify hashes are different
                assertNotEquals(hash1, hash2, 
                    "Each request should generate a different hash");
                
                // Verify first hash matches first plain token
                assertTrue(passwordEncoder.matches(plainToken1, hash1), 
                    "First hash should match first plain token");
                
                // Verify second hash matches second plain token
                assertTrue(passwordEncoder.matches(plainToken2, hash2), 
                    "Second hash should match second plain token");
                
                // Verify cross-matching doesn't work
                assertFalse(passwordEncoder.matches(plainToken1, hash2), 
                    "First plain token should not match second hash");
                assertFalse(passwordEncoder.matches(plainToken2, hash1), 
                    "Second plain token should not match first hash");
                
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
     * Property 19 (variant): Token validation uses hashed comparison
     * 
     * For any valid token, the validation process should use BCrypt comparison
     * rather than direct string comparison.
     * 
     * **Feature: admin-forgot-password, Property 19: Token hashing before storage**
     * **Validates: Requirements 6.2**
     */
    @Test
    public void tokenValidationUsesHashedComparison() {
        // Run property test with 100 iterations
        int tries = 100;
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
                
                // Get the stored hash
                List<PasswordResetTokenEntity> tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                String storedHash = tokens.get(0).getTokenHash();
                
                // Verify validation works with plain token
                assertTrue(passwordResetService.validateResetToken(plainToken), 
                    "Validation should succeed with plain token");
                
                // Verify validation fails with the hash itself
                assertFalse(passwordResetService.validateResetToken(storedHash), 
                    "Validation should fail when using the hash as input");
                
                // Verify validation fails with wrong token
                String wrongToken = "wrong-token-" + UUID.randomUUID();
                assertFalse(passwordResetService.validateResetToken(wrongToken), 
                    "Validation should fail with wrong token");
                
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
