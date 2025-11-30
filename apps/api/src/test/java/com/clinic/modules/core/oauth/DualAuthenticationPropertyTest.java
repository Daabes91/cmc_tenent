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
 * Property-based tests for dual authentication support.
 * These tests verify that patients with linked accounts can authenticate using either method.
 * 
 * Feature: patient-google-oauth
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DualAuthenticationPropertyTest {

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TenantEntity createTestTenant(String slug) {
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic " + slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }

    /**
     * Property 10: Dual authentication support
     * 
     * For any patient with linked accounts (both local and Google credentials),
     * authentication should succeed via either method.
     * 
     * **Feature: patient-google-oauth, Property 10: Dual authentication support**
     * **Validates: Requirements 4.3, 4.5**
     */
    @Test
    public void patientWithLinkedAccountsCanAuthenticateViaBothMethods() {
        TenantEntity testTenant = createTestTenant("dual-auth-test");
        String email = "dualauth@example.com";
        String password = "SecurePassword123";
        String googleId = "google-id-123456789012";
        String firstName = "John";
        String lastName = "Doe";
        // Create a patient with local authentication
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            email,
            "+1234567890",
            passwordEncoder.encode(password),
            LocalDate.of(1990, 1, 1)
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        PatientEntity patient = new PatientEntity(firstName, lastName, email, "+1234567890");
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(testTenant);
        patient = patientRepository.save(patient);

        // Verify local authentication works before linking
        Optional<PatientEntity> foundByEmail = patientRepository
            .findByTenantIdAndGlobalPatient_Email(testTenant.getId(), email);
        assertTrue(foundByEmail.isPresent(), "Should find patient by email before linking");
        assertEquals(patient.getId(), foundByEmail.get().getId());

        // Link Google account
        globalPatient.linkGoogleAccount(googleId, email);
        globalPatientRepository.save(globalPatient);

        patient.linkGoogleAccount(googleId);
        patientRepository.save(patient);

        // Verify both authentication methods work after linking
        
        // 1. Local authentication (by email)
        Optional<PatientEntity> foundByEmailAfterLink = patientRepository
            .findByTenantIdAndGlobalPatient_Email(testTenant.getId(), email);
        assertTrue(foundByEmailAfterLink.isPresent(), 
            "Should still find patient by email after linking Google account");
        assertEquals(patient.getId(), foundByEmailAfterLink.get().getId());
        
        // Verify password is still present
        GlobalPatientEntity reloadedGlobal = globalPatientRepository.findById(globalPatient.getId()).orElseThrow();
        assertNotNull(reloadedGlobal.getPasswordHash(), 
            "Password hash should still be present after linking Google account");
        
        // 2. Google authentication (by Google ID)
        Optional<PatientEntity> foundByGoogleId = patientRepository
            .findByGoogleIdAndTenantId(googleId, testTenant.getId());
        assertTrue(foundByGoogleId.isPresent(), 
            "Should find patient by Google ID after linking");
        assertEquals(patient.getId(), foundByGoogleId.get().getId());
        
        // Verify Google ID is present
        PatientEntity reloadedPatient = patientRepository.findById(patient.getId()).orElseThrow();
        assertEquals(googleId, reloadedPatient.getGoogleId(), 
            "Google ID should be stored in patient entity");
        
        // Verify auth provider is updated
        assertEquals(AuthProvider.BOTH, reloadedGlobal.getAuthProvider(),
            "Auth provider should be BOTH after linking");
        
        // Verify both credentials are present
        assertTrue(reloadedGlobal.hasLocalAuth(), 
            "Should have local auth after linking");
        assertTrue(reloadedGlobal.hasGoogleAuth(), 
            "Should have Google auth after linking");
    }

    /**
     * Property 10 (variant): Patient with only local auth cannot authenticate via Google
     * 
     * For any patient with only local credentials,
     * attempting to authenticate via Google should fail.
     * 
     * **Feature: patient-google-oauth, Property 10: Dual authentication support**
     * **Validates: Requirements 4.3, 4.5**
     */
    @Test
    public void patientWithOnlyLocalAuthCannotAuthenticateViaGoogle() {
        TenantEntity testTenant = createTestTenant("local-only-test");
        String email = "localonly@example.com";
        String password = "SecurePassword123";
        String googleId = "google-id-987654321098";
        String firstName = "Jane";
        String lastName = "Smith";
        // Create a patient with only local authentication
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            email,
            "+1234567890",
            passwordEncoder.encode(password),
            LocalDate.of(1990, 1, 1)
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        PatientEntity patient = new PatientEntity(firstName, lastName, email, "+1234567890");
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(testTenant);
        patient = patientRepository.save(patient);

        // Verify local authentication works
        Optional<PatientEntity> foundByEmail = patientRepository
            .findByTenantIdAndGlobalPatient_Email(testTenant.getId(), email);
        assertTrue(foundByEmail.isPresent(), "Should find patient by email");

        // Verify Google authentication fails
        Optional<PatientEntity> foundByGoogleId = patientRepository
            .findByGoogleIdAndTenantId(googleId, testTenant.getId());
        assertFalse(foundByGoogleId.isPresent(), 
            "Should not find patient by Google ID when not linked");
        
        // Verify auth provider is LOCAL
        assertEquals(AuthProvider.LOCAL, globalPatient.getAuthProvider(),
            "Auth provider should be LOCAL for local-only accounts");
    }

    /**
     * Property 10 (variant): Patient with only Google auth cannot authenticate via local
     * 
     * For any patient with only Google credentials,
     * they should not have a password hash.
     * 
     * **Feature: patient-google-oauth, Property 10: Dual authentication support**
     * **Validates: Requirements 4.3, 4.5**
     */
    @Test
    public void patientWithOnlyGoogleAuthHasNoPassword() {
        TenantEntity testTenant = createTestTenant("google-only-test");
        String email = "googleonly@example.com";
        String googleId = "google-id-111222333444";
        String firstName = "Bob";
        String lastName = "Johnson";
        // Create a patient with only Google authentication
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            googleId,
            email,
            firstName,
            lastName
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        PatientEntity patient = new PatientEntity(firstName, lastName, email, "+1234567890");
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(testTenant);
        patient.linkGoogleAccount(googleId);
        patient = patientRepository.save(patient);

        // Verify Google authentication works
        Optional<PatientEntity> foundByGoogleId = patientRepository
            .findByGoogleIdAndTenantId(googleId, testTenant.getId());
        assertTrue(foundByGoogleId.isPresent(), 
            "Should find patient by Google ID");

        // Verify no password hash
        GlobalPatientEntity reloadedGlobal = globalPatientRepository.findById(globalPatient.getId()).orElseThrow();
        assertNull(reloadedGlobal.getPasswordHash(), 
            "Password hash should be null for Google-only accounts");
        
        // Verify auth provider is GOOGLE
        assertEquals(AuthProvider.GOOGLE, reloadedGlobal.getAuthProvider(),
            "Auth provider should be GOOGLE for Google-only accounts");
        
        // Verify only Google auth is present
        assertFalse(reloadedGlobal.hasLocalAuth(), 
            "Should not have local auth for Google-only accounts");
        assertTrue(reloadedGlobal.hasGoogleAuth(), 
            "Should have Google auth for Google-only accounts");
    }

}
