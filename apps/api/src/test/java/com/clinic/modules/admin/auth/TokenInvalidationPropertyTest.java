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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for existing token invalidation.
 * 
 * **Feature: admin-forgot-password, Property 21: Existing token invalidation**
 * **Validates: Requirements 6.5**
 * 
 * This test verifies that:
 * 1. For any staff member with existing unused password reset tokens,
 *    generating a new token invalidates all previous unused tokens for that staff member
 * 2. Invalidated tokens are marked as used
 * 3. Only tokens for the specific staff member are invalidated
 * 
 * Note: This is implemented as a Spring Boot integration test that runs
 * property-based testing logic manually (100+ iterations).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TokenInvalidationPropertyTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Property 21: Existing token invalidation
     * 
     * For any staff member with existing unused password reset tokens,
     * generating a new token should invalidate all previous unused tokens for that staff member.
     * 
     * **Feature: admin-forgot-password, Property 21: Existing token invalidation**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void newTokenInvalidatesExistingTokens() {
        // Run property test with 100 iterations
        int tries = 100;
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
                
                // Verify first token exists and is unused
                List<PasswordResetTokenEntity> tokensAfterFirst = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokensAfterFirst.size(), 
                    "Should have one unused token after first request");
                assertFalse(tokensAfterFirst.get(0).isUsed(), 
                    "First token should not be marked as used");
                
                // Clear rate limit to allow second request
                passwordResetService.clearRateLimit(staff.getEmail(), tenant.getSlug());
                
                // Request second password reset
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(token2Opt.isPresent(), "Second token should be generated");
                
                // Verify only one unused token exists (the new one)
                List<PasswordResetTokenEntity> tokensAfterSecond = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, tokensAfterSecond.size(), 
                    "Should have only one unused token after second request");
                
                // Verify the first token was invalidated
                PasswordResetTokenEntity firstToken = tokenRepository
                    .findById(tokensAfterFirst.get(0).getId())
                    .orElseThrow();
                assertTrue(firstToken.isUsed(), 
                    "First token should be marked as used after second request");
                assertNotNull(firstToken.getUsedAt(), 
                    "First token should have a used_at timestamp");
                
                // Verify the second token is not used
                PasswordResetTokenEntity secondToken = tokensAfterSecond.get(0);
                assertFalse(secondToken.isUsed(), 
                    "Second token should not be marked as used");
                assertNull(secondToken.getUsedAt(), 
                    "Second token should not have a used_at timestamp");
                
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
     * Property 21 (variant): Multiple existing tokens are all invalidated
     * 
     * For any staff member with multiple existing unused tokens,
     * generating a new token should invalidate all of them.
     * 
     * **Feature: admin-forgot-password, Property 21: Existing token invalidation**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void newTokenInvalidatesAllExistingTokens() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Create multiple existing tokens manually (simulating multiple requests)
                int existingTokenCount = 2 + (int)(Math.random() * 4); // 2-5 tokens
                List<Long> existingTokenIds = new ArrayList<>();
                
                for (int j = 0; j < existingTokenCount; j++) {
                    String tokenHash = "$2a$10$" + UUID.randomUUID().toString().replace("-", "") + 
                                      UUID.randomUUID().toString().replace("-", "").substring(0, 28);
                    Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
                    
                    PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                        tokenHash, staff, tenant, expiresAt
                    );
                    PasswordResetTokenEntity savedToken = tokenRepository.saveAndFlush(token);
                    existingTokenIds.add(savedToken.getId());
                }
                
                // Verify all tokens are unused
                List<PasswordResetTokenEntity> unusedTokensBefore = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(existingTokenCount, unusedTokensBefore.size(), 
                    "Should have all tokens unused before new request");
                
                // Request new password reset
                Optional<String> newTokenOpt = passwordResetService.requestPasswordResetForTesting(
                    staff.getEmail(), tenant.getSlug()
                );
                assertTrue(newTokenOpt.isPresent(), "New token should be generated");
                
                // Verify only one unused token exists (the new one)
                List<PasswordResetTokenEntity> unusedTokensAfter = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(1, unusedTokensAfter.size(), 
                    "Should have only one unused token after new request");
                
                // Verify all existing tokens were invalidated
                for (Long tokenId : existingTokenIds) {
                    PasswordResetTokenEntity token = tokenRepository
                        .findById(tokenId)
                        .orElseThrow();
                    assertTrue(token.isUsed(), 
                        String.format("Existing token %d should be marked as used", tokenId));
                    assertNotNull(token.getUsedAt(), 
                        String.format("Existing token %d should have a used_at timestamp", tokenId));
                }
                
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
     * Property 21 (variant): Token invalidation is staff-specific
     * 
     * For any staff member, generating a new token should only invalidate
     * tokens for that specific staff member, not tokens for other staff members.
     * 
     * **Feature: admin-forgot-password, Property 21: Existing token invalidation**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void tokenInvalidationIsStaffSpecific() {
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
                assertTrue(token1Opt.isPresent(), "Token for staff1 should be generated");
                
                // Request password reset for staff2
                Optional<String> token2Opt = passwordResetService.requestPasswordResetForTesting(
                    staff2.getEmail(), tenant.getSlug()
                );
                assertTrue(token2Opt.isPresent(), "Token for staff2 should be generated");
                
                // Verify both staff have one unused token each
                List<PasswordResetTokenEntity> staff1Tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff1.getId());
                List<PasswordResetTokenEntity> staff2Tokens = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff2.getId());
                
                assertEquals(1, staff1Tokens.size(), "Staff1 should have one unused token");
                assertEquals(1, staff2Tokens.size(), "Staff2 should have one unused token");
                
                Long staff1TokenId = staff1Tokens.get(0).getId();
                Long staff2TokenId = staff2Tokens.get(0).getId();
                
                // Clear rate limit for staff1
                passwordResetService.clearRateLimit(staff1.getEmail(), tenant.getSlug());
                
                // Request new password reset for staff1
                Optional<String> newToken1Opt = passwordResetService.requestPasswordResetForTesting(
                    staff1.getEmail(), tenant.getSlug()
                );
                assertTrue(newToken1Opt.isPresent(), "New token for staff1 should be generated");
                
                // Verify staff1's old token was invalidated
                PasswordResetTokenEntity staff1OldToken = tokenRepository
                    .findById(staff1TokenId)
                    .orElseThrow();
                assertTrue(staff1OldToken.isUsed(), 
                    "Staff1's old token should be marked as used");
                
                // Verify staff2's token is still valid
                PasswordResetTokenEntity staff2Token = tokenRepository
                    .findById(staff2TokenId)
                    .orElseThrow();
                assertFalse(staff2Token.isUsed(), 
                    "Staff2's token should still be unused");
                assertNull(staff2Token.getUsedAt(), 
                    "Staff2's token should not have a used_at timestamp");
                
                // Verify staff2 still has one unused token
                List<PasswordResetTokenEntity> staff2TokensAfter = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff2.getId());
                assertEquals(1, staff2TokensAfter.size(), 
                    "Staff2 should still have one unused token");
                
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
     * Property 21 (variant): Direct invalidation method works correctly
     * 
     * For any staff member, calling invalidateExistingTokens should mark
     * all unused tokens as used.
     * 
     * **Feature: admin-forgot-password, Property 21: Existing token invalidation**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void directInvalidationMethodWorks() {
        // Run property test with 50 iterations
        int tries = 50;
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < tries; i++) {
            try {
                // Create test tenant
                TenantEntity tenant = createTestTenant();
                
                // Create test staff user
                StaffUser staff = createTestStaff(tenant);
                
                // Create multiple tokens manually
                int tokenCount = 2 + (int)(Math.random() * 4); // 2-5 tokens
                List<Long> tokenIds = new ArrayList<>();
                
                for (int j = 0; j < tokenCount; j++) {
                    String tokenHash = "$2a$10$" + UUID.randomUUID().toString().replace("-", "") + 
                                      UUID.randomUUID().toString().replace("-", "").substring(0, 28);
                    Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
                    
                    PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                        tokenHash, staff, tenant, expiresAt
                    );
                    PasswordResetTokenEntity savedToken = tokenRepository.saveAndFlush(token);
                    tokenIds.add(savedToken.getId());
                }
                
                // Verify all tokens are unused
                List<PasswordResetTokenEntity> unusedTokensBefore = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(tokenCount, unusedTokensBefore.size(), 
                    "Should have all tokens unused before invalidation");
                
                // Call invalidateExistingTokens
                passwordResetService.invalidateExistingTokens(staff.getId());
                
                // Flush to ensure database is updated
                tokenRepository.flush();
                
                // Verify no unused tokens remain
                List<PasswordResetTokenEntity> unusedTokensAfter = 
                    tokenRepository.findByStaffIdAndUsedFalse(staff.getId());
                assertEquals(0, unusedTokensAfter.size(), 
                    "Should have no unused tokens after invalidation");
                
                // Verify all tokens are marked as used
                for (Long tokenId : tokenIds) {
                    PasswordResetTokenEntity token = tokenRepository
                        .findById(tokenId)
                        .orElseThrow();
                    assertTrue(token.isUsed(), 
                        String.format("Token %d should be marked as used", tokenId));
                    assertNotNull(token.getUsedAt(), 
                        String.format("Token %d should have a used_at timestamp", tokenId));
                }
                
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
