package com.clinic.modules.core.oauth;

import com.clinic.modules.core.patient.*;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PatientGoogleAuthService.
 * These tests verify the core properties of the Google OAuth authentication flow.
 * 
 * Feature: patient-google-oauth
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PatientGoogleAuthServiceIntegrationTest {

    @Autowired
    private PatientGoogleAuthService patientGoogleAuthService;

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Property 5: New patient creation from Google OAuth
     * 
     * For any new Google user (no existing account), authenticating should create 
     * a patient record with data populated from Google user info.
     * 
     * **Feature: patient-google-oauth, Property 5: New patient creation from Google OAuth**
     * **Validates: Requirements 2.2, 2.3, 2.4**
     */
    @Test
    public void testNewPatientCreationFromGoogleOAuth() {
        // Create test tenant
        TenantEntity tenant = createTestTenant("test-clinic-new-patient");
        
        // Test data
        String googleId = "google-id-12345678901";
        String email = "newuser@example.com";
        String firstName = "John";
        String lastName = "Doe";
        
        // Authenticate with Google (should create new patient)
        PatientAuthResult result = patientGoogleAuthService.authenticateWithGoogle(
            googleId, email, firstName, lastName, tenant.getId()
        );
        
        // Verify result indicates new patient
        assertTrue(result.isNewPatient(), "Should indicate new patient creation");
        assertFalse(result.accountLinked(), "Should not indicate account linking");
        
        // Verify patient data matches Google user info
        assertEquals(email, result.email());
        assertEquals(firstName, result.firstName());
        assertEquals(lastName, result.lastName());
        
        // Verify patient exists in database
        PatientEntity patient = patientRepository.findById(result.patientId()).orElse(null);
        assertNotNull(patient);
        assertEquals(firstName, patient.getFirstName());
        assertEquals(lastName, patient.getLastName());
        assertEquals(googleId, patient.getGoogleId());
        
        // Verify global patient has correct auth provider
        GlobalPatientEntity globalPatient = globalPatientRepository.findById(result.globalPatientId()).orElse(null);
        assertNotNull(globalPatient);
        assertEquals(AuthProvider.GOOGLE, globalPatient.getAuthProvider());
        assertNull(globalPatient.getPasswordHash());
    }

    /**
     * Property 6: Multi-tenant isolation for Google accounts
     * 
     * For any Google ID used across multiple tenants, the system should create 
     * separate patient records for each tenant.
     * 
     * **Feature: patient-google-oauth, Property 6: Multi-tenant isolation for Google accounts**
     * **Validates: Requirements 3.1, 3.2, 3.4**
     */
    @Test
    public void testMultiTenantIsolationForGoogleAccounts() {
        // Create two tenants
        TenantEntity tenant1 = createTestTenant("clinic-1");
        TenantEntity tenant2 = createTestTenant("clinic-2");
        
        String googleId = "google-id-multitenant";
        String email = "user@example.com";
        
        // Authenticate in both tenants
        PatientAuthResult result1 = patientGoogleAuthService.authenticateWithGoogle(
            googleId, email, "John", "Doe", tenant1.getId()
        );
        
        PatientAuthResult result2 = patientGoogleAuthService.authenticateWithGoogle(
            googleId, email, "John", "Doe", tenant2.getId()
        );
        
        // Verify separate patient records
        assertNotEquals(result1.patientId(), result2.patientId());
        
        // Verify same global patient
        assertEquals(result1.globalPatientId(), result2.globalPatientId());
        
        // Verify tenant scoping
        PatientEntity patient1 = patientRepository.findById(result1.patientId()).orElse(null);
        PatientEntity patient2 = patientRepository.findById(result2.patientId()).orElse(null);
        
        assertNotNull(patient1);
        assertNotNull(patient2);
        assertEquals(tenant1.getId(), patient1.getTenant().getId());
        assertEquals(tenant2.getId(), patient2.getTenant().getId());
    }

    /**
     * Property 7: Patient queries are tenant-scoped
     * 
     * For any Google ID that exists in multiple tenants, querying by Google ID 
     * should return only the patient record for the current tenant.
     * 
     * **Feature: patient-google-oauth, Property 7: Patient queries are tenant-scoped**
     * **Validates: Requirements 3.3**
     */
    @Test
    public void testPatientQueriesAreTenantScoped() {
        // Create two tenants
        TenantEntity tenant1 = createTestTenant("clinic-a");
        TenantEntity tenant2 = createTestTenant("clinic-b");
        
        String googleId = "google-id-scoped";
        
        // Create patients in both tenants
        PatientAuthResult result1 = patientGoogleAuthService.authenticateWithGoogle(
            googleId, "user@example.com", "Jane", "Smith", tenant1.getId()
        );
        
        PatientAuthResult result2 = patientGoogleAuthService.authenticateWithGoogle(
            googleId, "user@example.com", "Jane", "Smith", tenant2.getId()
        );
        
        // Query in each tenant
        var patient1 = patientGoogleAuthService.findByGoogleIdAndTenant(googleId, tenant1.getId());
        var patient2 = patientGoogleAuthService.findByGoogleIdAndTenant(googleId, tenant2.getId());
        
        // Verify correct patients returned
        assertTrue(patient1.isPresent());
        assertTrue(patient2.isPresent());
        assertEquals(result1.patientId(), patient1.get().getId());
        assertEquals(result2.patientId(), patient2.get().getId());
        assertNotEquals(patient1.get().getId(), patient2.get().getId());
    }

    /**
     * Property 9: Account linking for existing patients
     * 
     * For any existing local patient account, authenticating via Google with the same email 
     * should link the Google ID to the existing record.
     * 
     * **Feature: patient-google-oauth, Property 9: Account linking for existing patients**
     * **Validates: Requirements 4.1, 4.2**
     */
    @Test
    public void testAccountLinkingForExistingPatients() {
        // Create tenant
        TenantEntity tenant = createTestTenant("clinic-link");
        
        String email = "existing@example.com";
        
        // Create existing local patient
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            email, null, "hashed-password", java.time.LocalDate.of(1990, 1, 1)
        );
        globalPatient = globalPatientRepository.save(globalPatient);
        
        PatientEntity localPatient = new PatientEntity("Alice", "Johnson", email, null);
        localPatient.setGlobalPatient(globalPatient);
        localPatient.setTenant(tenant);
        localPatient = patientRepository.save(localPatient);
        
        Long originalPatientId = localPatient.getId();
        
        // Authenticate with Google using same email
        String googleId = "google-id-link";
        PatientAuthResult result = patientGoogleAuthService.authenticateWithGoogle(
            googleId, email, "Alice", "Johnson", tenant.getId()
        );
        
        // Verify account was linked
        assertFalse(result.isNewPatient());
        assertTrue(result.accountLinked());
        assertEquals(originalPatientId, result.patientId());
        
        // Verify Google ID is linked
        PatientEntity updatedPatient = patientRepository.findById(originalPatientId).orElse(null);
        assertNotNull(updatedPatient);
        assertEquals(googleId, updatedPatient.getGoogleId());
        
        // Verify global patient has both auth methods
        GlobalPatientEntity updatedGlobalPatient = globalPatientRepository.findById(result.globalPatientId()).orElse(null);
        assertNotNull(updatedGlobalPatient);
        assertEquals(AuthProvider.BOTH, updatedGlobalPatient.getAuthProvider());
        assertNotNull(updatedGlobalPatient.getPasswordHash());
        assertEquals(googleId, updatedGlobalPatient.getGoogleId());
    }

    /**
     * Property 11: New account creation on email mismatch
     * 
     * For any Google authentication where the email doesn't match an existing patient, 
     * the system should create a new patient record.
     * 
     * **Feature: patient-google-oauth, Property 11: New account creation on email mismatch**
     * **Validates: Requirements 4.4**
     */
    @Test
    public void testNewAccountCreationOnEmailMismatch() {
        // Create tenant
        TenantEntity tenant = createTestTenant("clinic-mismatch");
        
        // Create existing patient with one email
        GlobalPatientEntity existingGlobal = new GlobalPatientEntity(
            "existing@example.com", null, "hashed-password", java.time.LocalDate.of(1990, 1, 1)
        );
        existingGlobal = globalPatientRepository.save(existingGlobal);
        
        PatientEntity existingPatient = new PatientEntity("Bob", "Brown", "existing@example.com", null);
        existingPatient.setGlobalPatient(existingGlobal);
        existingPatient.setTenant(tenant);
        existingPatient = patientRepository.save(existingPatient);
        
        Long existingPatientId = existingPatient.getId();
        
        // Authenticate with Google using different email
        PatientAuthResult result = patientGoogleAuthService.authenticateWithGoogle(
            "google-id-new", "different@example.com", "Charlie", "Davis", tenant.getId()
        );
        
        // Verify new patient was created
        assertTrue(result.isNewPatient());
        assertFalse(result.accountLinked());
        assertNotEquals(existingPatientId, result.patientId());
        
        // Verify both patients exist
        long patientCount = patientRepository.findAllByTenantId(tenant.getId()).size();
        assertEquals(2, patientCount);
        
        // Verify existing patient unchanged
        PatientEntity unchangedPatient = patientRepository.findById(existingPatientId).orElse(null);
        assertNotNull(unchangedPatient);
        assertEquals("existing@example.com", unchangedPatient.getEmail());
        assertNull(unchangedPatient.getGoogleId());
    }

    /**
     * Property 13: Tenant context scopes all operations
     * 
     * For any OAuth flow, all patient operations should be scoped to the tenant.
     * 
     * **Feature: patient-google-oauth, Property 13: Tenant context scopes all operations**
     * **Validates: Requirements 5.4**
     */
    @Test
    public void testTenantContextScopesAllOperations() {
        // Create two tenants
        TenantEntity tenant1 = createTestTenant("clinic-ctx1");
        TenantEntity tenant2 = createTestTenant("clinic-ctx2");
        
        String googleId = "google-id-context";
        
        // Authenticate in tenant 1
        PatientAuthResult result1 = patientGoogleAuthService.authenticateWithGoogle(
            googleId, "user@example.com", "Eve", "Wilson", tenant1.getId()
        );
        
        // Verify patient is scoped to tenant 1
        PatientEntity patient1 = patientRepository.findById(result1.patientId()).orElse(null);
        assertNotNull(patient1);
        assertEquals(tenant1.getId(), patient1.getTenant().getId());
        
        // Verify patient not accessible in tenant 2 context
        var notFound = patientRepository.findByIdAndTenantId(result1.patientId(), tenant2.getId());
        assertFalse(notFound.isPresent());
        
        // Verify Google ID lookup is tenant-scoped
        var foundInTenant1 = patientGoogleAuthService.findByGoogleIdAndTenant(googleId, tenant1.getId());
        var notFoundInTenant2 = patientGoogleAuthService.findByGoogleIdAndTenant(googleId, tenant2.getId());
        
        assertTrue(foundInTenant1.isPresent());
        assertFalse(notFoundInTenant2.isPresent());
        
        // Create patient in tenant 2
        PatientAuthResult result2 = patientGoogleAuthService.authenticateWithGoogle(
            googleId, "user@example.com", "Eve", "Wilson", tenant2.getId()
        );
        
        // Verify separate patients
        assertNotEquals(result1.patientId(), result2.patientId());
        
        // Verify complete isolation
        assertEquals(1, patientRepository.findAllByTenantId(tenant1.getId()).size());
        assertEquals(1, patientRepository.findAllByTenantId(tenant2.getId()).size());
    }

    // Helper methods

    private TenantEntity createTestTenant(String slug) {
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic " + slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }
}
