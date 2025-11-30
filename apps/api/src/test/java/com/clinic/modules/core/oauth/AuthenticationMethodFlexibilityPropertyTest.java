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
 * Property-based tests for authentication method flexibility.
 * These tests verify that the system supports authentication via local credentials only,
 * Google only, or both methods, and that patients can use their preferred method.
 * 
 * Feature: patient-google-oauth
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenticationMethodFlexibilityPropertyTest {

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
     * Property 19: Authentication method flexibility
     * 
     * For any patient, the system should support authentication via local credentials only,
     * Google only, or both methods.
     * 
     * This test verifies:
     * 1. Patients with only local credentials can authenticate via email/password
     * 2. Patients with only Google credentials can authenticate via Google
     * 3. Patients with both credentials can authenticate via either method
     * 4. Each authentication method is independent and works correctly
     * 
     * **Feature: patient-google-oauth, Property 19: Authentication method flexibility**
     * **Validates: Requirements 8.3, 8.4**
     */
    @Test
    public void patientWithOnlyLocalCredentialsCanAuthenticate() {
        String email = "localonly@example.com";
        String firstName = "Local";
        String lastName = "User";
        String password = "LocalPassword123";
        String phone = "+1111111111";
        String tenantSlug = "local-only-tenant";
        
        // Create a tenant
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Create a patient with only local authentication
        LocalDate dob = LocalDate.of(1990, 1, 1);
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
            email, phone, dob, password
        );
        
        PatientEntity patient = new PatientEntity(firstName, lastName, email, phone);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient = patientRepository.save(patient);
        
        // Verify patient has only local auth
        assertTrue(globalPatient.hasLocalAuth(), 
            "Patient should have local auth");
        assertFalse(globalPatient.hasGoogleAuth(), 
            "Patient should not have Google auth");
        assertEquals(AuthProvider.LOCAL, globalPatient.getAuthProvider(), 
            "Auth provider should be LOCAL");
        
        // Verify local authentication works
        Optional<GlobalPatientEntity> foundByEmail = globalPatientService.findByEmail(email);
        assertTrue(foundByEmail.isPresent(), 
            "Should find patient by email");
        
        boolean validPassword = globalPatientService.validatePassword(globalPatient, password);
        assertTrue(validPassword, 
            "Password should validate successfully");
        
        // Verify tenant-scoped patient can be found by email
        Optional<PatientEntity> foundTenantPatient = patientRepository
            .findByTenantIdAndGlobalPatient_Email(tenant.getId(), email);
        assertTrue(foundTenantPatient.isPresent(), 
            "Should find tenant patient by email");
        assertEquals(patient.getId(), foundTenantPatient.get().getId(), 
            "Found patient should match");
        
        // Verify Google authentication is not available
        assertNull(globalPatient.getGoogleId(), 
            "Google ID should be null");
        assertNull(patient.getGoogleId(), 
            "Patient Google ID should be null");
    }

    /**
     * Property 19 (variant): Patient with only Google credentials can authenticate
     * 
     * For any patient with only Google credentials,
     * they should be able to authenticate via Google OAuth.
     * 
     * **Feature: patient-google-oauth, Property 19: Authentication method flexibility**
     * **Validates: Requirements 8.3, 8.4**
     */
    @Test
    public void patientWithOnlyGoogleCredentialsCanAuthenticate() {
        String email = "googleonly@example.com";
        String firstName = "Google";
        String lastName = "User";
        String googleId = "google-id-999888777";
        String phone = "+2222222222";
        String tenantSlug = "google-only-tenant";
        
        // Create a tenant
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Create a patient with only Google authentication
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            googleId,
            email,
            firstName,
            lastName
        );
        globalPatient = globalPatientRepository.save(globalPatient);
        
        PatientEntity patient = new PatientEntity(firstName, lastName, email, phone);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient.linkGoogleAccount(googleId);
        patient = patientRepository.save(patient);
        
        // Verify patient has only Google auth
        assertFalse(globalPatient.hasLocalAuth(), 
            "Patient should not have local auth");
        assertTrue(globalPatient.hasGoogleAuth(), 
            "Patient should have Google auth");
        assertEquals(AuthProvider.GOOGLE, globalPatient.getAuthProvider(), 
            "Auth provider should be GOOGLE");
        
        // Verify Google authentication works
        Optional<PatientEntity> foundByGoogleId = patientRepository
            .findByGoogleIdAndTenantId(googleId, tenant.getId());
        assertTrue(foundByGoogleId.isPresent(), 
            "Should find patient by Google ID");
        assertEquals(patient.getId(), foundByGoogleId.get().getId(), 
            "Found patient should match");
        
        // Verify Google ID is stored correctly
        assertEquals(googleId, globalPatient.getGoogleId(), 
            "Global patient should have Google ID");
        assertEquals(googleId, patient.getGoogleId(), 
            "Tenant patient should have Google ID");
        
        // Verify local authentication is not available
        assertNull(globalPatient.getPasswordHash(), 
            "Password hash should be null");
        
        // Verify can still find by email (for account linking scenarios)
        Optional<GlobalPatientEntity> foundByEmail = globalPatientService.findByEmail(email);
        assertTrue(foundByEmail.isPresent(), 
            "Should find patient by email");
    }

    /**
     * Property 19 (variant): Patient with both credentials can authenticate via either method
     * 
     * For any patient with both local and Google credentials,
     * they should be able to authenticate via either method.
     * 
     * **Feature: patient-google-oauth, Property 19: Authentication method flexibility**
     * **Validates: Requirements 8.3, 8.4**
     */
    @Test
    public void patientWithBothCredentialsCanAuthenticateViaEitherMethod() {
        String email = "both@example.com";
        String firstName = "Both";
        String lastName = "User";
        String password = "BothPassword123";
        String googleId = "google-id-111222333";
        String phone = "+3333333333";
        String tenantSlug = "both-auth-tenant";
        
        // Create a tenant
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Create a patient with local authentication first
        LocalDate dob = LocalDate.of(1990, 1, 1);
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
            email, phone, dob, password
        );
        
        PatientEntity patient = new PatientEntity(firstName, lastName, email, phone);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient = patientRepository.save(patient);
        
        // Link Google account
        globalPatient.linkGoogleAccount(googleId, email);
        globalPatient = globalPatientRepository.save(globalPatient);
        
        patient.linkGoogleAccount(googleId);
        patient = patientRepository.save(patient);
        
        // Verify patient has both auth methods
        assertTrue(globalPatient.hasLocalAuth(), 
            "Patient should have local auth");
        assertTrue(globalPatient.hasGoogleAuth(), 
            "Patient should have Google auth");
        assertEquals(AuthProvider.BOTH, globalPatient.getAuthProvider(), 
            "Auth provider should be BOTH");
        
        // Test 1: Local authentication works
        Optional<GlobalPatientEntity> foundByEmail = globalPatientService.findByEmail(email);
        assertTrue(foundByEmail.isPresent(), 
            "Should find patient by email");
        
        boolean validPassword = globalPatientService.validatePassword(globalPatient, password);
        assertTrue(validPassword, 
            "Password should validate successfully");
        
        Optional<PatientEntity> foundByEmailTenant = patientRepository
            .findByTenantIdAndGlobalPatient_Email(tenant.getId(), email);
        assertTrue(foundByEmailTenant.isPresent(), 
            "Should find tenant patient by email");
        
        // Test 2: Google authentication works
        Optional<PatientEntity> foundByGoogleId = patientRepository
            .findByGoogleIdAndTenantId(googleId, tenant.getId());
        assertTrue(foundByGoogleId.isPresent(), 
            "Should find patient by Google ID");
        assertEquals(patient.getId(), foundByGoogleId.get().getId(), 
            "Found patient should match");
        
        // Test 3: Both methods point to the same patient
        assertEquals(foundByEmailTenant.get().getId(), foundByGoogleId.get().getId(), 
            "Both auth methods should return the same patient");
        
        // Test 4: Verify both credentials are present
        assertNotNull(globalPatient.getPasswordHash(), 
            "Password hash should be present");
        assertNotNull(globalPatient.getGoogleId(), 
            "Google ID should be present");
        assertEquals(googleId, globalPatient.getGoogleId(), 
            "Google ID should match");
    }

    /**
     * Property 19 (variant): Different patients can use different authentication methods
     * 
     * For any set of patients in the same tenant,
     * each can use their own preferred authentication method independently.
     * 
     * **Feature: patient-google-oauth, Property 19: Authentication method flexibility**
     * **Validates: Requirements 8.3, 8.4**
     */
    @Test
    public void differentPatientsCanUseDifferentAuthMethods() {
        String tenantSlug = "mixed-auth-tenant";
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Patient 1: Local only
        String email1 = "patient1@example.com";
        String password1 = "Password1";
        LocalDate dob = LocalDate.of(1990, 1, 1);
        GlobalPatientEntity globalPatient1 = globalPatientService.findOrCreateGlobalPatient(
            email1, "+1111111111", dob, password1
        );
        PatientEntity patient1 = new PatientEntity("Patient", "One", email1, "+1111111111");
        patient1.setGlobalPatient(globalPatient1);
        patient1.setTenant(tenant);
        patient1 = patientRepository.save(patient1);
        
        // Patient 2: Google only
        String email2 = "patient2@example.com";
        String googleId2 = "google-id-222";
        GlobalPatientEntity globalPatient2 = new GlobalPatientEntity(
            googleId2, email2, "Patient", "Two"
        );
        globalPatient2 = globalPatientRepository.save(globalPatient2);
        PatientEntity patient2 = new PatientEntity("Patient", "Two", email2, "+2222222222");
        patient2.setGlobalPatient(globalPatient2);
        patient2.setTenant(tenant);
        patient2.linkGoogleAccount(googleId2);
        patient2 = patientRepository.save(patient2);
        
        // Patient 3: Both
        String email3 = "patient3@example.com";
        String password3 = "Password3";
        String googleId3 = "google-id-333";
        GlobalPatientEntity globalPatient3 = globalPatientService.findOrCreateGlobalPatient(
            email3, "+3333333333", dob, password3
        );
        PatientEntity patient3 = new PatientEntity("Patient", "Three", email3, "+3333333333");
        patient3.setGlobalPatient(globalPatient3);
        patient3.setTenant(tenant);
        patient3 = patientRepository.save(patient3);
        
        globalPatient3.linkGoogleAccount(googleId3, email3);
        globalPatient3 = globalPatientRepository.save(globalPatient3);
        patient3.linkGoogleAccount(googleId3);
        patient3 = patientRepository.save(patient3);
        
        // Verify Patient 1 (local only)
        assertEquals(AuthProvider.LOCAL, globalPatient1.getAuthProvider());
        assertTrue(globalPatient1.hasLocalAuth());
        assertFalse(globalPatient1.hasGoogleAuth());
        assertTrue(globalPatientService.validatePassword(globalPatient1, password1));
        
        // Verify Patient 2 (Google only)
        assertEquals(AuthProvider.GOOGLE, globalPatient2.getAuthProvider());
        assertFalse(globalPatient2.hasLocalAuth());
        assertTrue(globalPatient2.hasGoogleAuth());
        Optional<PatientEntity> found2 = patientRepository
            .findByGoogleIdAndTenantId(googleId2, tenant.getId());
        assertTrue(found2.isPresent());
        assertEquals(patient2.getId(), found2.get().getId());
        
        // Verify Patient 3 (both)
        assertEquals(AuthProvider.BOTH, globalPatient3.getAuthProvider());
        assertTrue(globalPatient3.hasLocalAuth());
        assertTrue(globalPatient3.hasGoogleAuth());
        assertTrue(globalPatientService.validatePassword(globalPatient3, password3));
        Optional<PatientEntity> found3 = patientRepository
            .findByGoogleIdAndTenantId(googleId3, tenant.getId());
        assertTrue(found3.isPresent());
        assertEquals(patient3.getId(), found3.get().getId());
        
        // Verify all patients are in the same tenant
        assertEquals(tenant.getId(), patient1.getTenant().getId());
        assertEquals(tenant.getId(), patient2.getTenant().getId());
        assertEquals(tenant.getId(), patient3.getTenant().getId());
        
        // Verify patients are independent
        assertNotEquals(patient1.getId(), patient2.getId());
        assertNotEquals(patient1.getId(), patient3.getId());
        assertNotEquals(patient2.getId(), patient3.getId());
    }

    /**
     * Property 19 (variant): Authentication method can be upgraded from local to both
     * 
     * For any patient with local credentials,
     * they should be able to add Google authentication without losing local auth.
     * 
     * **Feature: patient-google-oauth, Property 19: Authentication method flexibility**
     * **Validates: Requirements 8.3, 8.4**
     */
    @Test
    public void authMethodCanBeUpgradedFromLocalToBoth() {
        String email = "upgrade@example.com";
        String firstName = "Upgrade";
        String lastName = "User";
        String password = "UpgradePassword123";
        String googleId = "google-id-upgrade";
        String phone = "+4444444444";
        String tenantSlug = "upgrade-tenant";
        
        // Create a tenant
        TenantEntity tenant = createTenant(tenantSlug);
        
        // Create a patient with only local authentication
        LocalDate dob = LocalDate.of(1990, 1, 1);
        GlobalPatientEntity globalPatient = globalPatientService.findOrCreateGlobalPatient(
            email, phone, dob, password
        );
        
        PatientEntity patient = new PatientEntity(firstName, lastName, email, phone);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient = patientRepository.save(patient);
        
        // Verify initial state: LOCAL only
        assertEquals(AuthProvider.LOCAL, globalPatient.getAuthProvider(), 
            "Initial auth provider should be LOCAL");
        assertTrue(globalPatient.hasLocalAuth(), 
            "Should have local auth initially");
        assertFalse(globalPatient.hasGoogleAuth(), 
            "Should not have Google auth initially");
        
        // Verify local auth works before upgrade
        boolean passwordWorksBeforeUpgrade = globalPatientService.validatePassword(globalPatient, password);
        assertTrue(passwordWorksBeforeUpgrade, 
            "Password should work before upgrade");
        
        // Upgrade: Add Google authentication
        globalPatient.linkGoogleAccount(googleId, email);
        globalPatient = globalPatientRepository.save(globalPatient);
        
        patient.linkGoogleAccount(googleId);
        patient = patientRepository.save(patient);
        
        // Verify upgraded state: BOTH
        assertEquals(AuthProvider.BOTH, globalPatient.getAuthProvider(), 
            "Auth provider should be BOTH after upgrade");
        assertTrue(globalPatient.hasLocalAuth(), 
            "Should still have local auth after upgrade");
        assertTrue(globalPatient.hasGoogleAuth(), 
            "Should have Google auth after upgrade");
        
        // Verify local auth still works after upgrade
        boolean passwordWorksAfterUpgrade = globalPatientService.validatePassword(globalPatient, password);
        assertTrue(passwordWorksAfterUpgrade, 
            "Password should still work after upgrade");
        
        // Verify Google auth works after upgrade
        Optional<PatientEntity> foundByGoogleId = patientRepository
            .findByGoogleIdAndTenantId(googleId, tenant.getId());
        assertTrue(foundByGoogleId.isPresent(), 
            "Should find patient by Google ID after upgrade");
        assertEquals(patient.getId(), foundByGoogleId.get().getId(), 
            "Found patient should match after upgrade");
        
        // Verify both credentials are present
        assertNotNull(globalPatient.getPasswordHash(), 
            "Password hash should still be present");
        assertNotNull(globalPatient.getGoogleId(), 
            "Google ID should be present");
    }

    // Helper methods
    
    private TenantEntity createTenant(String slug) {
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic " + slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }
}
