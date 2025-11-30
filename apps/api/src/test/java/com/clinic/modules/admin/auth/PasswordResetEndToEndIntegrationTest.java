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
 * Feature: admin-forgot-password, Property 17: Password reset round trip
 * 
 * Property: For any staff member who successfully resets their password, 
 * logging in with the new password should authenticate successfully
 * 
 * Validates: Requirements 5.4
 * 
 * This test verifies the complete end-to-end flow:
 * 1. Request password reset
 * 2. Validate token
 * 3. Reset password
 * 4. Verify new password works for authentication
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PasswordResetEndToEndIntegrationTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Property 17: Password reset round trip
     * 
     * For any staff member who successfully resets their password,
     * logging in with the new password should authenticate successfully.
     * 
     * **Feature: admin-forgot-password, Property 17: Password reset round trip**
     * **Validates: Requirements 5.4**
     */
    @Test
    public void passwordResetRoundTrip() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity testTenant = createTestTenant();
                
                // Create test staff user with known password
                String originalPassword = "OriginalPassword123!";
                StaffUser testStaff = createTestStaff(testTenant, originalPassword);
                String originalPasswordHash = testStaff.getPasswordHash();
                
                // Clear rate limit
                passwordResetService.clearRateLimit(testStaff.getEmail(), testTenant.getSlug());
                
                // Generate a random new password (at least 8 characters)
                String newPassword = generateRandomPassword();
                
                // Step 1: Request password reset and get token
                Optional<String> tokenOpt = passwordResetService.requestPasswordResetForTesting(
                    testStaff.getEmail(), 
                    testTenant.getSlug()
                );
                assertTrue(tokenOpt.isPresent(), "Token should be generated");
                String token = tokenOpt.get();

                // Step 2: Validate token
                boolean isValid = passwordResetService.validateResetToken(token);
                assertTrue(isValid, "Token should be valid");

                // Step 3: Reset password
                boolean resetSuccess = passwordResetService.resetPassword(token, newPassword);
                assertTrue(resetSuccess, "Password reset should succeed");

                // Step 4: Verify new password works for authentication
                // Reload staff from database to get updated password hash
                StaffUser updatedStaff = staffUserRepository.findById(testStaff.getId()).orElseThrow();
                
                // Verify the new password matches the stored hash
                boolean passwordMatches = passwordEncoder.matches(newPassword, updatedStaff.getPasswordHash());
                assertTrue(passwordMatches, 
                    "New password should match the stored hash (authentication should succeed)");

                // Verify the old password no longer works
                boolean oldPasswordMatches = passwordEncoder.matches(originalPassword, updatedStaff.getPasswordHash());
                assertFalse(oldPasswordMatches, 
                    "Old password should not match the stored hash");

                // Verify password hash was actually changed
                assertNotEquals(originalPasswordHash, updatedStaff.getPasswordHash(),
                    "Password hash should be different from original");
                
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
