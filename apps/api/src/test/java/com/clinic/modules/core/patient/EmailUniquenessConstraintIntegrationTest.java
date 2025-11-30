package com.clinic.modules.core.patient;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for database constraints on email uniqueness.
 * 
 * This test verifies that the database constraints are properly enforced:
 * 1. UNIQUE(tenant_id, email) constraint on patients table
 * 2. UNIQUE(tenant_id, google_id) constraint on patients table
 * 
 * Validates: Requirements 3.4, 3.5
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmailUniquenessConstraintIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private TenantEntity tenant1;
    private TenantEntity tenant2;

    @BeforeEach
    public void setup() {
        // Create two test tenants
        tenant1 = createTenant("constraint-test-tenant-1");
        tenant2 = createTenant("constraint-test-tenant-2");
    }

    @Test
    public void testEmailUniquenessConstraintWithinTenant() {
        String email = "test@example.com";

        // Create first patient with this email in tenant1
        GlobalPatientEntity globalPatient1 = createGlobalPatient(email);
        PatientEntity patient1 = createPatient(tenant1, globalPatient1, "John", "Doe", email);
        patientRepository.saveAndFlush(patient1);

        // Attempt to create second patient with same email in tenant1 should fail
        GlobalPatientEntity globalPatient2 = createGlobalPatient("different-" + email);
        PatientEntity patient2 = createPatient(tenant1, globalPatient2, "Jane", "Smith", email);

        // This should throw DataIntegrityViolationException due to unique constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            patientRepository.saveAndFlush(patient2);
        }, "Should not allow duplicate email in same tenant");
    }

    @Test
    public void testEmailCanBeUsedAcrossDifferentTenants() {
        String email = "shared@example.com";

        // Create patient with this email in tenant1
        GlobalPatientEntity globalPatient1 = createGlobalPatient(email);
        PatientEntity patient1 = createPatient(tenant1, globalPatient1, "John", "Doe", email);
        patientRepository.saveAndFlush(patient1);

        // Create patient with same email in tenant2 - should succeed
        GlobalPatientEntity globalPatient2 = createGlobalPatient("alt-" + email);
        PatientEntity patient2 = createPatient(tenant2, globalPatient2, "Jane", "Smith", email);
        PatientEntity savedPatient2 = patientRepository.saveAndFlush(patient2);

        // Verify both patients exist
        assertNotNull(savedPatient2.getId());
        assertEquals(email, savedPatient2.getEmail());
        assertNotEquals(patient1.getTenant().getId(), savedPatient2.getTenant().getId());
    }

    @Test
    public void testEmailConstraintIsCaseInsensitive() {
        String email = "test@example.com";
        String uppercaseEmail = "TEST@EXAMPLE.COM";

        // Create first patient with lowercase email
        GlobalPatientEntity globalPatient1 = createGlobalPatient(email);
        PatientEntity patient1 = createPatient(tenant1, globalPatient1, "John", "Doe", email);
        patientRepository.saveAndFlush(patient1);

        // Attempt to create second patient with uppercase email should fail
        GlobalPatientEntity globalPatient2 = createGlobalPatient("different-" + email);
        PatientEntity patient2 = createPatient(tenant1, globalPatient2, "Jane", "Smith", uppercaseEmail);

        // This should throw DataIntegrityViolationException
        assertThrows(DataIntegrityViolationException.class, () -> {
            patientRepository.saveAndFlush(patient2);
        }, "Should not allow duplicate email with different case in same tenant");
    }

    @Test
    public void testGoogleIdUniquenessConstraintWithinTenant() {
        String googleId = "google-123456";
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // Create first patient with this Google ID in tenant1
        GlobalPatientEntity globalPatient1 = createGlobalPatient(email1);
        globalPatient1.setGoogleId(googleId);
        globalPatientRepository.saveAndFlush(globalPatient1);
        
        PatientEntity patient1 = createPatient(tenant1, globalPatient1, "John", "Doe", email1);
        patient1.setGoogleId(googleId);
        patientRepository.saveAndFlush(patient1);

        // Attempt to create second patient with same Google ID in tenant1 should fail
        GlobalPatientEntity globalPatient2 = createGlobalPatient(email2);
        PatientEntity patient2 = createPatient(tenant1, globalPatient2, "Jane", "Smith", email2);
        patient2.setGoogleId(googleId);

        // This should throw DataIntegrityViolationException due to unique constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            patientRepository.saveAndFlush(patient2);
        }, "Should not allow duplicate Google ID in same tenant");
    }

    @Test
    public void testGoogleIdCanBeUsedAcrossDifferentTenants() {
        String googleId = "google-789012";
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // Create patient with this Google ID in tenant1
        GlobalPatientEntity globalPatient1 = createGlobalPatient(email1);
        globalPatient1.setGoogleId(googleId);
        globalPatientRepository.saveAndFlush(globalPatient1);
        
        PatientEntity patient1 = createPatient(tenant1, globalPatient1, "John", "Doe", email1);
        patient1.setGoogleId(googleId);
        patientRepository.saveAndFlush(patient1);

        // Create patient with same Google ID in tenant2 - should succeed
        GlobalPatientEntity globalPatient2 = createGlobalPatient(email2);
        PatientEntity patient2 = createPatient(tenant2, globalPatient2, "Jane", "Smith", email2);
        patient2.setGoogleId(googleId);
        PatientEntity savedPatient2 = patientRepository.saveAndFlush(patient2);

        // Verify both patients exist with same Google ID but different tenants
        assertNotNull(savedPatient2.getId());
        assertEquals(googleId, savedPatient2.getGoogleId());
        assertNotEquals(patient1.getTenant().getId(), savedPatient2.getTenant().getId());
    }

    @Test
    public void testNullEmailsAreAllowed() {
        // Multiple patients can have null emails in the same tenant
        GlobalPatientEntity globalPatient1 = createGlobalPatient("patient1@example.com");
        PatientEntity patient1 = createPatient(tenant1, globalPatient1, "John", "Doe", null);
        patientRepository.saveAndFlush(patient1);

        GlobalPatientEntity globalPatient2 = createGlobalPatient("patient2@example.com");
        PatientEntity patient2 = createPatient(tenant1, globalPatient2, "Jane", "Smith", null);
        PatientEntity savedPatient2 = patientRepository.saveAndFlush(patient2);

        // Both should succeed
        assertNotNull(savedPatient2.getId());
        assertNull(savedPatient2.getEmail());
    }

    @Test
    public void testNullGoogleIdsAreAllowed() {
        // Multiple patients can have null Google IDs in the same tenant
        GlobalPatientEntity globalPatient1 = createGlobalPatient("patient1@example.com");
        PatientEntity patient1 = createPatient(tenant1, globalPatient1, "John", "Doe", "john@example.com");
        patient1.setGoogleId(null);
        patientRepository.saveAndFlush(patient1);

        GlobalPatientEntity globalPatient2 = createGlobalPatient("patient2@example.com");
        PatientEntity patient2 = createPatient(tenant1, globalPatient2, "Jane", "Smith", "jane@example.com");
        patient2.setGoogleId(null);
        PatientEntity savedPatient2 = patientRepository.saveAndFlush(patient2);

        // Both should succeed
        assertNotNull(savedPatient2.getId());
        assertNull(savedPatient2.getGoogleId());
    }

    // ========== Helper Methods ==========

    private TenantEntity createTenant(String slug) {
        String uniqueSlug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        TenantEntity tenant = new TenantEntity(uniqueSlug, "Test Tenant " + slug);
        tenant.setCustomDomain(slug + ".example.com");
        return tenantRepository.save(tenant);
    }

    private GlobalPatientEntity createGlobalPatient(String email) {
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
                email,
                "+1234567890",
                "hashed_password",
                LocalDate.of(1990, 1, 1)
        );
        return globalPatientRepository.save(globalPatient);
    }

    private PatientEntity createPatient(TenantEntity tenant, GlobalPatientEntity globalPatient,
                                       String firstName, String lastName, String email) {
        PatientEntity patient = new PatientEntity(firstName, lastName, email, "+1234567890");
        patient.setTenant(tenant);
        patient.setGlobalPatient(globalPatient);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return patient;
    }
}
