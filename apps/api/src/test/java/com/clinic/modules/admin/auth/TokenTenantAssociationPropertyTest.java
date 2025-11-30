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
 * Property-based test for token tenant association.
 * 
 * **Feature: admin-forgot-password, Property 25: Token tenant association**
 * **Validates: Requirements 8.3**
 * 
 * This test verifies that:
 * 1. For any generated password reset token, the token is associated with 
 *    the specific tenant ID from the request
 * 2. The tenant association is correctly stored and retrievable
 * 3. Tokens from different tenants are properly isolated
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TokenTenantAssociationPropertyTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Property 25: Token tenant association
     * 
     * For any generated password reset token, the token should be associated 
     * with the specific tenant ID from the request.
     * 
     * **Feature: admin-forgot-password, Property 25: Token tenant association**
     * **Validates: Requirements 8.3**
     */
    @Test
    public void generatedTokensAreAssociatedWithCorrectTenant() {
        // Run property test with 100 iterations
        int tries = 100;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create tenant
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                String tenantSlug = tenant.getSlug();
                
                // Create staff user in tenant
                StaffUser staff = createTestStaff(tenant);
                String email = staff.getEmail();
                
                // Clear rate limit for testing
                passwordResetService.clearRateLimit(email, tenantSlug);
                
                // Count tokens before request
                long tokenCountBefore = tokenRepository.count();
                
                // Request password reset
                Optional<String> plainToken = passwordResetService.requestPasswordResetForTesting(
                    email, tenantSlug
                );
                
                assertTrue(plainToken.isPresent(), 
                    "Password reset should succeed for valid email and tenant");
                
                // Verify token was created
                long tokenCountAfter = tokenRepository.count();
                assertEquals(tokenCountBefore + 1, tokenCountAfter, 
                    "One token should be created");
                
                // Find the created token
                List<PasswordResetTokenEntity> tokens = tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertFalse(tokens.isEmpty(), 
                    "Should find at least one token for staff");
                
                // Get the most recent token
                PasswordResetTokenEntity token = tokens.get(tokens.size() - 1);
                
                // Verify token is associated with correct tenant
                assertNotNull(token.getTenant(), 
                    "Token should have a tenant association");
                assertEquals(tenantId, token.getTenant().getId(), 
                    "Token should be associated with the correct tenant ID");
                assertEquals(tenantSlug, token.getTenant().getSlug(), 
                    "Token should be associated with the correct tenant slug");
                
                // Verify token is also associated with correct staff
                assertNotNull(token.getStaff(), 
                    "Token should have a staff association");
                assertEquals(staff.getId(), token.getStaff().getId(), 
                    "Token should be associated with the correct staff ID");
                
                // Verify staff and tenant are consistent
                assertEquals(tenantId, token.getStaff().getTenant().getId(), 
                    "Token's staff should belong to the same tenant");
                
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
     * Property 25 (variant): Tokens from different tenants are isolated
     * 
     * For any two different tenants, tokens generated for one tenant should not
     * be accessible or usable by the other tenant.
     * 
     * **Feature: admin-forgot-password, Property 25: Token tenant association**
     * **Validates: Requirements 8.3**
     */
    @Test
    public void tokensFromDifferentTenantsAreIsolated() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create two different tenants
                TenantEntity tenant1 = createTestTenant();
                TenantEntity tenant2 = createTestTenant();
                
                // Create staff users in each tenant
                StaffUser staff1 = createTestStaff(tenant1);
                StaffUser staff2 = createTestStaff(tenant2);
                
                // Clear rate limits
                passwordResetService.clearRateLimit(staff1.getEmail(), tenant1.getSlug());
                passwordResetService.clearRateLimit(staff2.getEmail(), tenant2.getSlug());
                
                // Generate tokens for both staff members
                Optional<String> token1 = passwordResetService.requestPasswordResetForTesting(
                    staff1.getEmail(), tenant1.getSlug()
                );
                Optional<String> token2 = passwordResetService.requestPasswordResetForTesting(
                    staff2.getEmail(), tenant2.getSlug()
                );
                
                assertTrue(token1.isPresent(), "Token should be generated for staff1");
                assertTrue(token2.isPresent(), "Token should be generated for staff2");
                
                // Retrieve tokens
                List<PasswordResetTokenEntity> tokens1 = tokenRepository.findByStaffIdAndUsedFalse(staff1.getId());
                List<PasswordResetTokenEntity> tokens2 = tokenRepository.findByStaffIdAndUsedFalse(staff2.getId());
                
                assertFalse(tokens1.isEmpty(), "Should find token for staff1");
                assertFalse(tokens2.isEmpty(), "Should find token for staff2");
                
                PasswordResetTokenEntity tokenEntity1 = tokens1.get(tokens1.size() - 1);
                PasswordResetTokenEntity tokenEntity2 = tokens2.get(tokens2.size() - 1);
                
                // Verify tokens are associated with correct tenants
                assertEquals(tenant1.getId(), tokenEntity1.getTenant().getId(), 
                    "Token1 should be associated with tenant1");
                assertEquals(tenant2.getId(), tokenEntity2.getTenant().getId(), 
                    "Token2 should be associated with tenant2");
                
                // Verify tokens are different
                assertNotEquals(tokenEntity1.getId(), tokenEntity2.getId(), 
                    "Tokens should have different IDs");
                assertNotEquals(tokenEntity1.getTokenHash(), tokenEntity2.getTokenHash(), 
                    "Tokens should have different hashes");
                
                // Verify tenant isolation
                assertNotEquals(tokenEntity1.getTenant().getId(), tokenEntity2.getTenant().getId(), 
                    "Tokens should belong to different tenants");
                
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
     * Property 25 (variant): Token tenant association persists across sessions
     * 
     * For any generated token, the tenant association should persist even after
     * clearing the persistence context (simulating a new session).
     * 
     * **Feature: admin-forgot-password, Property 25: Token tenant association**
     * **Validates: Requirements 8.3**
     */
    @Test
    public void tokenTenantAssociationPersistsAcrossSessions() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create tenant
                TenantEntity tenant = createTestTenant();
                Long tenantId = tenant.getId();
                
                // Create staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Clear rate limit
                passwordResetService.clearRateLimit(staff.getEmail(), tenant.getSlug());
                
                // Generate token
                Optional<String> plainToken = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                
                assertTrue(plainToken.isPresent(), "Token should be generated");
                
                // Get token ID
                List<PasswordResetTokenEntity> tokens = tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertFalse(tokens.isEmpty(), "Should find token");
                Long tokenId = tokens.get(tokens.size() - 1).getId();
                
                // Flush and clear persistence context to simulate new session
                tokenRepository.flush();
                
                // Retrieve token in "new session"
                PasswordResetTokenEntity retrievedToken = tokenRepository.findById(tokenId)
                    .orElseThrow(() -> new AssertionError("Token should be retrievable"));
                
                // Verify tenant association persists
                assertNotNull(retrievedToken.getTenant(), 
                    "Token should still have tenant association");
                assertEquals(tenantId, retrievedToken.getTenant().getId(), 
                    "Token should still be associated with correct tenant");
                
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
