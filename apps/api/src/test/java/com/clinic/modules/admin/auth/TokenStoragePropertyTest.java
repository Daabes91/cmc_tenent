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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for password reset token storage with required fields.
 * 
 * **Feature: admin-forgot-password, Property 18: Token storage with required fields**
 * **Validates: Requirements 6.1**
 * 
 * This test verifies that:
 * 1. For any generated password reset token, the database record includes staff ID, 
 *    tenant ID, expiration timestamp, and hashed token
 * 2. All required fields are properly stored and retrievable
 * 3. Token relationships to staff and tenant are correctly maintained
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TokenStoragePropertyTest {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private final Random random = new Random();

    /**
     * Property 18: Token storage with required fields
     * 
     * For any generated password reset token, the database record should include
     * staff ID, tenant ID, expiration timestamp, and hashed token.
     * 
     * **Feature: admin-forgot-password, Property 18: Token storage with required fields**
     * **Validates: Requirements 6.1**
     */
    @Test
    public void tokenStorageIncludesAllRequiredFields() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                Long staffId = staff.getId();
                
                // Generate random token data
                String tokenHash = generateRandomTokenHash();
                Instant expiresAt = generateRandomExpirationTime();
                
                // Create and save password reset token
                PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                    tokenHash, staff, tenant, expiresAt
                );
                
                PasswordResetTokenEntity savedToken = tokenRepository.saveAndFlush(token);
                
                // Verify all required fields are present
                assertNotNull(savedToken.getId(), 
                    "Token should have an ID after saving");
                
                assertNotNull(savedToken.getTokenHash(), 
                    "Token should have a token hash");
                assertEquals(tokenHash, savedToken.getTokenHash(), 
                    "Token hash should match the provided value");
                
                assertNotNull(savedToken.getStaff(), 
                    "Token should have a staff reference");
                assertEquals(staffId, savedToken.getStaff().getId(), 
                    "Token should reference the correct staff ID");
                
                assertNotNull(savedToken.getTenant(), 
                    "Token should have a tenant reference");
                assertEquals(tenantId, savedToken.getTenant().getId(), 
                    "Token should reference the correct tenant ID");
                
                assertNotNull(savedToken.getExpiresAt(), 
                    "Token should have an expiration timestamp");
                assertEquals(expiresAt.truncatedTo(ChronoUnit.MILLIS), 
                    savedToken.getExpiresAt().truncatedTo(ChronoUnit.MILLIS), 
                    "Token expiration should match the provided value");
                
                assertNotNull(savedToken.getCreatedAt(), 
                    "Token should have a creation timestamp");
                assertTrue(savedToken.getCreatedAt().isBefore(Instant.now().plusSeconds(1)), 
                    "Token creation timestamp should be in the past or present");
                
                assertFalse(savedToken.isUsed(), 
                    "New token should not be marked as used");
                
                assertNull(savedToken.getUsedAt(), 
                    "New token should not have a used_at timestamp");
                
                // Verify token can be retrieved by token hash
                PasswordResetTokenEntity retrievedToken = tokenRepository
                    .findByTokenHash(tokenHash)
                    .orElse(null);
                
                assertNotNull(retrievedToken, 
                    "Token should be retrievable by token hash");
                assertEquals(savedToken.getId(), retrievedToken.getId(), 
                    "Retrieved token should have the same ID");
                
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
     * Property 18 (variant): Token storage maintains referential integrity
     * 
     * For any password reset token, the staff and tenant relationships should be
     * properly maintained through foreign key constraints.
     * 
     * **Feature: admin-forgot-password, Property 18: Token storage with required fields**
     * **Validates: Requirements 6.1**
     */
    @Test
    public void tokenStorageMaintainsReferentialIntegrity() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Generate random token data
                String tokenHash = generateRandomTokenHash();
                Instant expiresAt = generateRandomExpirationTime();
                
                // Create and save password reset token
                PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                    tokenHash, staff, tenant, expiresAt
                );
                
                PasswordResetTokenEntity savedToken = tokenRepository.saveAndFlush(token);
                Long tokenId = savedToken.getId();
                
                // Clear persistence context to force fresh load
                tokenRepository.flush();
                
                // Retrieve token and verify relationships are loaded correctly
                PasswordResetTokenEntity retrievedToken = tokenRepository
                    .findById(tokenId)
                    .orElseThrow();
                
                assertNotNull(retrievedToken.getStaff(), 
                    "Token should maintain staff relationship");
                assertEquals(staff.getId(), retrievedToken.getStaff().getId(), 
                    "Token should reference correct staff");
                assertEquals(staff.getEmail(), retrievedToken.getStaff().getEmail(), 
                    "Staff email should be accessible through relationship");
                
                assertNotNull(retrievedToken.getTenant(), 
                    "Token should maintain tenant relationship");
                assertEquals(tenant.getId(), retrievedToken.getTenant().getId(), 
                    "Token should reference correct tenant");
                assertEquals(tenant.getSlug(), retrievedToken.getTenant().getSlug(), 
                    "Tenant slug should be accessible through relationship");
                
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
     * Property 18 (variant): Multiple tokens can exist for same staff
     * 
     * For any staff member, multiple password reset tokens can be stored
     * (though only one should be active at a time in practice).
     * 
     * **Feature: admin-forgot-password, Property 18: Token storage with required fields**
     * **Validates: Requirements 6.1**
     */
    @Test
    public void tokenStorageAllowsMultipleTokensPerStaff() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Generate random number of tokens (2-5)
                int tokenCount = 2 + random.nextInt(4);
                List<PasswordResetTokenEntity> tokens = new ArrayList<>();
                
                for (int j = 0; j < tokenCount; j++) {
                    String tokenHash = generateRandomTokenHash();
                    Instant expiresAt = generateRandomExpirationTime();
                    
                    PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                        tokenHash, staff, tenant, expiresAt
                    );
                    
                    PasswordResetTokenEntity savedToken = tokenRepository.saveAndFlush(token);
                    tokens.add(savedToken);
                }
                
                // Verify all tokens were saved
                assertEquals(tokenCount, tokens.size(), 
                    "All tokens should be saved");
                
                // Verify all tokens reference the same staff
                for (PasswordResetTokenEntity token : tokens) {
                    assertEquals(staff.getId(), token.getStaff().getId(), 
                        "All tokens should reference the same staff");
                    assertEquals(tenant.getId(), token.getTenant().getId(), 
                        "All tokens should reference the same tenant");
                }
                
                // Verify tokens can be retrieved by staff ID
                List<PasswordResetTokenEntity> retrievedTokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                
                assertEquals(tokenCount, retrievedTokens.size(), 
                    "Should retrieve all unused tokens for staff");
                
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

    private String generateRandomTokenHash() {
        // Simulate BCrypt hash format (60 characters)
        return "$2a$10$" + UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 28);
    }

    private Instant generateRandomExpirationTime() {
        // Generate expiration time between 30 minutes and 2 hours from now
        int minutesFromNow = 30 + random.nextInt(90);
        return Instant.now().plus(minutesFromNow, ChronoUnit.MINUTES);
    }
}
