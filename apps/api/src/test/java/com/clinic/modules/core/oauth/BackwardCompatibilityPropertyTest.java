package com.clinic.modules.core.oauth;

import com.clinic.modules.core.patient.*;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for backward compatibility with local authentication.
 * These tests verify that the existing email/password authentication continues to work
 * after Google OAuth is enabled, and that patients with only local credentials
 * can still authenticate successfully.
 * 
 * Feature: patient-google-oauth
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BackwardCompatibilityPropertyTest {

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private GlobalPatientService globalPatientService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Property 18: Backward compatibility with local authentication
     * 
     * For any patient with local credentials (with or without Google),
     * email/password authentication should continue to function correctly.
     * 
     * This test verifies:
     * 1. Patients with only local credentials can authenticate
     * 2. Password validation works correctly
     * 3. Patient lookup by email works correctly
     * 4. Local authentication is unaffected by Google OAuth feature
     * 
     * **Feature: patient-google-oauth, Property 18: Backward compatibility with local authentication**
     * **Validates: Requirements 8.1, 8.2**
     */
    @Test
    public void localAuthenticationContinuesToWork() {
        String email = "localauth@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String password = "SecurePassword123";
        String phone = "+1234567890";
        String tenantSlug = "local-auth-test";
        // Create a tenant
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Create a patient with local authentication
        LocalDate dob = LocalDate.of(1990, 1, 1);
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
            email, phone, dob, password
        );
        
        // Verify global patient was created with local auth
        assertNotNull(globalPatient, "Global patient should be created");
        assertNotNull(globalPatient.getId(), "Global patient should have an ID");
        assertEquals(email, globalPatient.getEmail(), "Email should match");
        assertNotNull(globalPatient.getPasswordHash(), "Password hash should be present");
        assertEquals(AuthProvider.LOCAL, globalPatient.getAuthProvider(), 
            "Auth provider should be LOCAL");
        
        // Create tenant-scoped patient
        PatientEntity patient = new PatientEntity(firstName, lastName, email, phone);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient = patientRepository.save(patient);
        
        // Test 1: Find patient by email (simulating login lookup)
        Optional<GlobalPatientEntity> foundByEmail = globalPatientService.findByEmail(email);
        assertTrue(foundByEmail.isPresent(), 
            "Should find global patient by email");
        assertEquals(globalPatient.getId(), foundByEmail.get().getId(), 
            "Found patient should match created patient");
        
        // Test 2: Validate correct password
        boolean validPassword = globalPatientService.validatePassword(globalPatient, password);
        assertTrue(validPassword, 
            "Correct password should validate successfully");
        
        // Test 3: Reject incorrect password
        String wrongPassword = password + "wrong";
        boolean invalidPassword = globalPatientService.validatePassword(globalPatient, wrongPassword);
        assertFalse(invalidPassword, 
            "Incorrect password should fail validation");
        
        // Test 4: Find tenant-scoped patient by email
        Optional<PatientEntity> foundTenantPatient = patientRepository
            .findByTenantIdAndGlobalPatient_Email(tenant.getId(), email);
        assertTrue(foundTenantPatient.isPresent(), 
            "Should find tenant-scoped patient by email");
        assertEquals(patient.getId(), foundTenantPatient.get().getId(), 
            "Found tenant patient should match created patient");
        
        // Test 5: Verify patient has local auth capabilities
        assertTrue(globalPatient.hasLocalAuth(), 
            "Patient should have local auth");
        assertFalse(globalPatient.hasGoogleAuth(), 
            "Patient should not have Google auth");
        
        // Test 6: Verify auth provider is LOCAL
        assertEquals(AuthProvider.LOCAL, globalPatient.getAuthProvider(), 
            "Auth provider should remain LOCAL");
    }

    /**
     * Property 18 (variant): Local authentication works after Google account is linked
     * 
     * For any patient who links a Google account to their existing local account,
     * the original email/password authentication should continue to work.
     * 
     * This ensures that adding Google OAuth doesn't break existing authentication.
     * 
     * **Feature: patient-google-oauth, Property 18: Backward compatibility with local authentication**
     * **Validates: Requirements 8.1, 8.2**
     */
    @Test
    public void localAuthWorksAfterLinkingGoogle() {
        String email = "linkedauth@example.com";
        String firstName = "Jane";
        String lastName = "Smith";
        String password = "SecurePassword456";
        String phone = "+1987654321";
        String tenantSlug = "linked-auth-test";
        String googleId = "google-id-123456789";
        // Create a tenant
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Create a patient with local authentication
        LocalDate dob = LocalDate.of(1990, 1, 1);
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
            email, phone, dob, password
        );
        
        PatientEntity patient = new PatientEntity(firstName, lastName, email, phone);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient = patientRepository.save(patient);
        
        // Verify local auth works before linking
        boolean passwordWorksBeforeLinking = globalPatientService.validatePassword(globalPatient, password);
        assertTrue(passwordWorksBeforeLinking, 
            "Password should work before linking Google account");
        
        // Link Google account
        globalPatient.linkGoogleAccount(googleId, email);
        globalPatient = globalPatientRepository.save(globalPatient);
        
        patient.linkGoogleAccount(googleId);
        patient = patientRepository.save(patient);
        
        // Verify auth provider changed to BOTH
        assertEquals(AuthProvider.BOTH, globalPatient.getAuthProvider(), 
            "Auth provider should be BOTH after linking");
        
        // Test 1: Local authentication still works after linking
        boolean passwordWorksAfterLinking = globalPatientService.validatePassword(globalPatient, password);
        assertTrue(passwordWorksAfterLinking, 
            "Password should still work after linking Google account");
        
        // Test 2: Password hash is still present
        assertNotNull(globalPatient.getPasswordHash(), 
            "Password hash should still be present after linking");
        
        // Test 3: Can still find by email
        Optional<GlobalPatientEntity> foundByEmail = globalPatientService.findByEmail(email);
        assertTrue(foundByEmail.isPresent(), 
            "Should still find patient by email after linking");
        
        // Test 4: Can still find tenant-scoped patient by email
        Optional<PatientEntity> foundTenantPatient = patientRepository
            .findByTenantIdAndGlobalPatient_Email(tenant.getId(), email);
        assertTrue(foundTenantPatient.isPresent(), 
            "Should still find tenant patient by email after linking");
        
        // Test 5: Verify both auth methods are available
        assertTrue(globalPatient.hasLocalAuth(), 
            "Should still have local auth after linking");
        assertTrue(globalPatient.hasGoogleAuth(), 
            "Should have Google auth after linking");
        
        // Test 6: Wrong password still fails
        String wrongPassword = password + "wrong";
        boolean invalidPassword = globalPatientService.validatePassword(globalPatient, wrongPassword);
        assertFalse(invalidPassword, 
            "Wrong password should still fail after linking");
    }

    /**
     * Property 18 (variant): Multiple patients with local auth in same tenant
     * 
     * For any set of patients with local credentials in the same tenant,
     * each should authenticate independently without interference.
     * 
     * This ensures multi-tenant isolation and proper password validation.
     * 
     * **Feature: patient-google-oauth, Property 18: Backward compatibility with local authentication**
     * **Validates: Requirements 8.1, 8.2**
     */
    @Test
    public void multipleLocalPatientsAuthenticateIndependently() {
        String email1 = "patient1@example.com";
        String email2 = "patient2@example.com";
        String firstName1 = "Alice";
        String firstName2 = "Bob";
        String password1 = "Password123";
        String password2 = "Password456";
        String tenantSlug = "multi-patient-test";
        
        // Create a tenant
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Create first patient
        LocalDate dob = LocalDate.of(1990, 1, 1);
        GlobalPatientEntity globalPatient1 = globalPatientService.findOrCreateGlobalPatient(
            email1, "+1234567890", dob, password1
        );
        
        PatientEntity patient1 = new PatientEntity(firstName1, "Smith", email1, "+1234567890");
        patient1.setGlobalPatient(globalPatient1);
        patient1.setTenant(tenant);
        patient1 = patientRepository.save(patient1);
        
        // Create second patient
        GlobalPatientEntity globalPatient2 = globalPatientService.findOrCreateGlobalPatient(
            email2, "+0987654321", dob, password2
        );
        
        PatientEntity patient2 = new PatientEntity(firstName2, "Jones", email2, "+0987654321");
        patient2.setGlobalPatient(globalPatient2);
        patient2.setTenant(tenant);
        patient2 = patientRepository.save(patient2);
        
        // Test 1: Each patient can authenticate with their own password
        assertTrue(globalPatientService.validatePassword(globalPatient1, password1), 
            "Patient 1 should authenticate with password 1");
        assertTrue(globalPatientService.validatePassword(globalPatient2, password2), 
            "Patient 2 should authenticate with password 2");
        
        // Test 2: Each patient cannot authenticate with the other's password
        assertFalse(globalPatientService.validatePassword(globalPatient1, password2), 
            "Patient 1 should not authenticate with password 2");
        assertFalse(globalPatientService.validatePassword(globalPatient2, password1), 
            "Patient 2 should not authenticate with password 1");
        
        // Test 3: Each patient can be found by their email
        Optional<GlobalPatientEntity> found1 = globalPatientService.findByEmail(email1);
        Optional<GlobalPatientEntity> found2 = globalPatientService.findByEmail(email2);
        
        assertTrue(found1.isPresent(), "Should find patient 1 by email");
        assertTrue(found2.isPresent(), "Should find patient 2 by email");
        assertEquals(globalPatient1.getId(), found1.get().getId(), "Found patient 1 should match");
        assertEquals(globalPatient2.getId(), found2.get().getId(), "Found patient 2 should match");
        
        // Test 4: Both patients are in the same tenant
        Optional<PatientEntity> tenantPatient1 = patientRepository
            .findByTenantIdAndGlobalPatient_Email(tenant.getId(), email1);
        Optional<PatientEntity> tenantPatient2 = patientRepository
            .findByTenantIdAndGlobalPatient_Email(tenant.getId(), email2);
        
        assertTrue(tenantPatient1.isPresent(), "Should find tenant patient 1");
        assertTrue(tenantPatient2.isPresent(), "Should find tenant patient 2");
        assertEquals(tenant.getId(), tenantPatient1.get().getTenant().getId(), 
            "Patient 1 should be in correct tenant");
        assertEquals(tenant.getId(), tenantPatient2.get().getTenant().getId(), 
            "Patient 2 should be in correct tenant");
    }

    // Helper methods
    
    private TenantEntity createTenant(String slug) {
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic " + slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }
}
