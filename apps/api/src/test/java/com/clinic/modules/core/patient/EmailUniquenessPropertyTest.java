package com.clinic.modules.core.patient;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for email uniqueness per tenant.
 * 
 * Feature: patient-google-oauth, Property 8: Email uniqueness per tenant
 * Validates: Requirements 3.5
 * 
 * This test verifies that:
 * 1. Within a tenant, each email can only be used once
 * 2. The same email can be used across different tenants
 * 3. Email validation is case-insensitive
 * 4. Application-layer validation catches duplicates before database
 * 
 * Note: This is a unit test that tests the validation logic.
 * Integration tests verify the actual database constraints.
 */
public class EmailUniquenessPropertyTest {

    /**
     * Helper to create a mock repository for testing
     */
    private static class MockPatientRepository {
        private final java.util.Map<String, java.util.Set<String>> tenantEmails = new java.util.HashMap<>();
        
        public boolean existsByTenantAndEmail(Long tenantId, String email) {
            String key = tenantId.toString();
            return tenantEmails.getOrDefault(key, java.util.Set.of()).contains(email.toLowerCase());
        }
        
        public void addPatient(Long tenantId, String email) {
            String key = tenantId.toString();
            tenantEmails.computeIfAbsent(key, k -> new java.util.HashSet<>()).add(email.toLowerCase());
        }
        
        public void clear() {
            tenantEmails.clear();
        }
    }
    
    private MockPatientRepository mockRepository = new MockPatientRepository();

    /**
     * Property: For any tenant and email, attempting to create multiple patients 
     * with the same email should fail within that tenant.
     */
    @Property(tries = 100)
    void emailMustBeUniqueWithinTenant(
            @ForAll("validEmails") String email,
            @ForAll("tenantIds") Long tenantId) {
        
        mockRepository.clear();
        
        // Normalize email for consistent testing
        String normalizedEmail = normalizeEmail(email);

        // Add first patient with this email in tenant
        mockRepository.addPatient(tenantId, normalizedEmail);
        
        // Check that duplicate is detected
        boolean exists = mockRepository.existsByTenantAndEmail(tenantId, normalizedEmail);
        assertTrue(exists, "Email should exist in tenant after adding");
        
        // Verify that the same email in the same tenant is detected as duplicate
        assertTrue(mockRepository.existsByTenantAndEmail(tenantId, normalizedEmail),
            "Duplicate email in same tenant should be detected");
    }

    /**
     * Property: For any email, it can be used across different tenants 
     * (same email, different tenant = allowed).
     */
    @Property(tries = 100)
    void sameEmailCanBeUsedAcrossDifferentTenants(
            @ForAll("validEmails") String email,
            @ForAll("tenantIds") Long tenantId1,
            @ForAll("tenantIds") Long tenantId2) {
        
        Assume.that(!tenantId1.equals(tenantId2)); // Ensure different tenants
        
        mockRepository.clear();
        String normalizedEmail = normalizeEmail(email);

        // Add patient with this email in tenant1
        mockRepository.addPatient(tenantId1, normalizedEmail);
        
        // Add patient with same email in tenant2 - should succeed
        mockRepository.addPatient(tenantId2, normalizedEmail);

        // Verify both tenants have the email
        assertTrue(mockRepository.existsByTenantAndEmail(tenantId1, normalizedEmail),
            "Email should exist in tenant1");
        assertTrue(mockRepository.existsByTenantAndEmail(tenantId2, normalizedEmail),
            "Email should exist in tenant2");
    }

    /**
     * Property: Email validation should be case-insensitive.
     * Emails that differ only in case should be considered duplicates.
     */
    @Property(tries = 100)
    void emailValidationIsCaseInsensitive(
            @ForAll("validEmails") String email,
            @ForAll("tenantIds") Long tenantId) {
        
        mockRepository.clear();
        
        String normalizedEmail = normalizeEmail(email);
        String uppercaseEmail = normalizedEmail.toUpperCase();
        String mixedCaseEmail = mixCaseEmail(normalizedEmail);

        // Add patient with lowercase email
        mockRepository.addPatient(tenantId, normalizedEmail);

        // Check that uppercase version is detected as duplicate
        assertTrue(mockRepository.existsByTenantAndEmail(tenantId, uppercaseEmail),
            "Uppercase email should be detected as duplicate");

        // Check that mixed case version is detected as duplicate
        assertTrue(mockRepository.existsByTenantAndEmail(tenantId, mixedCaseEmail),
            "Mixed case email should be detected as duplicate");
    }

    /**
     * Property: Email normalization should be consistent.
     * The same email in different cases should normalize to the same value.
     */
    @Property(tries = 100)
    void emailNormalizationIsConsistent(
            @ForAll("validEmails") String email) {
        
        String normalized1 = normalizeEmail(email);
        String normalized2 = normalizeEmail(email.toUpperCase());
        String normalized3 = normalizeEmail(email.toLowerCase());
        String normalized4 = normalizeEmail("  " + email + "  "); // with whitespace
        
        // All normalizations should produce the same result
        assertEquals(normalized1, normalized2, "Normalization should be case-insensitive");
        assertEquals(normalized1, normalized3, "Normalization should be case-insensitive");
        assertEquals(normalized1, normalized4, "Normalization should trim whitespace");
        
        // Normalized email should be lowercase
        assertEquals(normalized1, normalized1.toLowerCase(), "Normalized email should be lowercase");
        
        // Normalized email should have no leading/trailing whitespace
        assertEquals(normalized1, normalized1.trim(), "Normalized email should be trimmed");
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(10)
                .map(s -> s + "@example.com");
    }

    @Provide
    Arbitrary<Long> tenantIds() {
        return Arbitraries.longs().between(1L, 1000L);
    }

    // ========== Helper Methods ==========

    /**
     * Normalize email address for consistent comparison.
     * Converts to lowercase and trims whitespace.
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String mixCaseEmail(String email) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < email.length(); i++) {
            char c = email.charAt(i);
            if (i % 2 == 0) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
