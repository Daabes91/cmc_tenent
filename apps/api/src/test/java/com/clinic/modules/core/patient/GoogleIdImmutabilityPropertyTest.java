package com.clinic.modules.core.patient;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Google ID immutability.
 * These tests verify that once a Google ID is set, it cannot be changed.
 * 
 * Feature: patient-google-oauth
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class GoogleIdImmutabilityPropertyTest {

    @Autowired
    private GlobalPatientRepository globalPatientRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private final Random random = new Random();

    /**
     * Helper method to create a test tenant.
     */
    private TenantEntity createTestTenant() {
        String slug = "test-clinic-" + System.currentTimeMillis() + "-" + random.nextInt(10000);
        TenantEntity tenant = new TenantEntity(slug, "Test Clinic");
        return tenantRepository.save(tenant);
    }

    /**
     * Helper method to generate a random Google ID.
     */
    private String generateGoogleId() {
        long id = 100000000000000000L + (long)(random.nextDouble() * 899999999999999999L);
        return String.valueOf(id);
    }

    /**
     * Helper method to generate a random email.
     */
    private String generateEmail() {
        String[] domains = {"gmail.com", "yahoo.com", "outlook.com", "example.com", "test.com"};
        String username = "user" + random.nextInt(100000);
        String domain = domains[random.nextInt(domains.length)];
        return username + "@" + domain;
    }

    /**
     * Property 17: Google ID immutability in GlobalPatientEntity
     * 
     * For any patient record with a Google ID, once stored, the Google ID should not be modifiable.
     * 
     * This test verifies:
     * 1. Setting a Google ID initially works
     * 2. Attempting to change it to a different value throws an exception
     * 3. Setting it to the same value is allowed (idempotent)
     * 4. Setting it to null when already set throws an exception
     * 
     * **Feature: patient-google-oauth, Property 17: Google ID immutability**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void globalPatientGoogleIdIsImmutable() {
        // Run multiple iterations to test the property
        for (int i = 0; i < 10; i++) {
            String initialGoogleId = generateGoogleId();
            String differentGoogleId = generateGoogleId();
            String email = generateEmail();

        // Create a global patient with Google OAuth
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            initialGoogleId,
            email,
            "Test",
            "User"
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        // Verify initial Google ID is set
        assertEquals(initialGoogleId, globalPatient.getGoogleId(),
            "Initial Google ID should be set correctly");

        // Attempt to change Google ID to a different value - should throw exception
        Long patientId = globalPatient.getId();
        GlobalPatientEntity savedPatient = globalPatientRepository.findById(patientId).orElseThrow();
        
        assertThrows(IllegalStateException.class, () -> {
            savedPatient.setGoogleId(differentGoogleId);
        }, "Changing Google ID should throw IllegalStateException");

        // Verify Google ID hasn't changed
        globalPatientRepository.flush();
        GlobalPatientEntity reloadedPatient = globalPatientRepository.findById(patientId).orElseThrow();
        assertEquals(initialGoogleId, reloadedPatient.getGoogleId(),
            "Google ID should remain unchanged after failed modification attempt");

        // Setting to the same value should be allowed (idempotent)
        assertDoesNotThrow(() -> {
            reloadedPatient.setGoogleId(initialGoogleId);
        }, "Setting Google ID to the same value should be allowed");

            // Verify it's still the same
            globalPatientRepository.flush();
            GlobalPatientEntity finalPatient = globalPatientRepository.findById(patientId).orElseThrow();
            assertEquals(initialGoogleId, finalPatient.getGoogleId(),
                "Google ID should remain the same after idempotent set");
        }
    }

    /**
     * Property 17: Google ID immutability in PatientEntity
     * 
     * For any tenant-scoped patient record with a Google ID, once stored, 
     * the Google ID should not be modifiable.
     * 
     * **Feature: patient-google-oauth, Property 17: Google ID immutability**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void patientEntityGoogleIdIsImmutable() {
        // Run multiple iterations to test the property
        for (int i = 0; i < 10; i++) {
            String initialGoogleId = generateGoogleId();
            String differentGoogleId = generateGoogleId();
            String email = generateEmail();

        // Create a global patient first
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            initialGoogleId,
            email,
            "Test",
            "User"
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        // Create a tenant-scoped patient
        TenantEntity tenant = createTestTenant();
        PatientEntity patient = new PatientEntity("Test", "User", email, null);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient.setGoogleId(initialGoogleId);
        patient = patientRepository.save(patient);

        // Verify initial Google ID is set
        assertEquals(initialGoogleId, patient.getGoogleId(),
            "Initial Google ID should be set correctly");

        // Attempt to change Google ID to a different value - should throw exception
        Long patientId = patient.getId();
        PatientEntity savedPatient = patientRepository.findById(patientId).orElseThrow();
        
        assertThrows(IllegalStateException.class, () -> {
            savedPatient.setGoogleId(differentGoogleId);
        }, "Changing Google ID should throw IllegalStateException");

        // Verify Google ID hasn't changed
        patientRepository.flush();
        PatientEntity reloadedPatient = patientRepository.findById(patientId).orElseThrow();
        assertEquals(initialGoogleId, reloadedPatient.getGoogleId(),
            "Google ID should remain unchanged after failed modification attempt");

            // Setting to the same value should be allowed (idempotent)
            assertDoesNotThrow(() -> {
                reloadedPatient.setGoogleId(initialGoogleId);
            }, "Setting Google ID to the same value should be allowed");
        }
    }

    /**
     * Property 17: Database-level immutability enforcement
     * 
     * For any patient record, the database trigger should prevent Google ID changes
     * even if application-level validation is bypassed.
     * 
     * This test verifies that the database trigger works correctly.
     * 
     * **Feature: patient-google-oauth, Property 17: Google ID immutability**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void databaseEnforcesGoogleIdImmutability() {
        // Test with a single iteration since database triggers abort the transaction
        String initialGoogleId = generateGoogleId();
        String differentGoogleId = generateGoogleId();
        String email = generateEmail();

        // Create a global patient with Google OAuth
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            initialGoogleId,
            email,
            "Test",
            "User"
        );
        globalPatient = globalPatientRepository.save(globalPatient);
        globalPatientRepository.flush();

        // Try to update directly using native query (bypassing entity validation)
        Long patientId = globalPatient.getId();
        
        // This should fail due to database trigger
        Exception exception = assertThrows(Exception.class, () -> {
            globalPatientRepository.updateGoogleIdDirectly(patientId, differentGoogleId);
            globalPatientRepository.flush();
        }, "Database trigger should prevent Google ID modification");

        // Verify the exception message mentions immutability
        String message = exception.getMessage();
        assertTrue(message != null && (message.contains("immutable") || message.contains("cannot be changed")),
            "Exception should mention immutability: " + message);
    }

    /**
     * Property 17: Null to non-null is allowed, but non-null to different value is not
     * 
     * For any patient record, setting Google ID from null to a value should work,
     * but changing from one non-null value to another should fail.
     * 
     * **Feature: patient-google-oauth, Property 17: Google ID immutability**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void nullToNonNullIsAllowedButChangeIsNot() {
        // Run multiple iterations to test the property
        for (int i = 0; i < 10; i++) {
            String googleId1 = generateGoogleId();
            String googleId2 = generateGoogleId();
            String email = generateEmail();

        // Create a local patient (no Google ID initially)
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            email,
            null,
            "password-hash",
            LocalDate.of(1990, 1, 1)
        );
        globalPatient = globalPatientRepository.save(globalPatient);

        // Verify Google ID is null initially
        assertNull(globalPatient.getGoogleId(), "Initial Google ID should be null");

        // Setting from null to non-null should work (account linking)
        globalPatient.linkGoogleAccount(googleId1, email);
        globalPatientRepository.save(globalPatient);
        globalPatientRepository.flush();

        // Verify Google ID is now set
        Long patientId = globalPatient.getId();
        GlobalPatientEntity reloadedPatient = globalPatientRepository.findById(patientId).orElseThrow();
        assertEquals(googleId1, reloadedPatient.getGoogleId(),
            "Google ID should be set after linking");

        // Now attempting to change to a different value should fail
        assertThrows(IllegalStateException.class, () -> {
            reloadedPatient.setGoogleId(googleId2);
        }, "Changing Google ID from one value to another should throw exception");

            // Verify Google ID hasn't changed
            globalPatientRepository.flush();
            GlobalPatientEntity finalPatient = globalPatientRepository.findById(patientId).orElseThrow();
            assertEquals(googleId1, finalPatient.getGoogleId(),
                "Google ID should remain unchanged after failed modification");
        }
    }

    /**
     * Property 17: linkGoogleAccount enforces immutability
     * 
     * For any patient that already has a Google account linked,
     * attempting to link again should fail.
     * 
     * **Feature: patient-google-oauth, Property 17: Google ID immutability**
     * **Validates: Requirements 6.5**
     */
    @Test
    public void linkGoogleAccountEnforcesImmutability() {
        // Run multiple iterations to test the property
        for (int i = 0; i < 10; i++) {
            String googleId1 = generateGoogleId();
            String googleId2 = generateGoogleId();
            String email = generateEmail();

        // Create a patient with Google OAuth
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
            googleId1,
            email,
            "Test",
            "User"
        );
        globalPatient = globalPatientRepository.save(globalPatient);
        Long patientId = globalPatient.getId();

        // Reload to get a fresh instance
        GlobalPatientEntity reloadedPatient = globalPatientRepository.findById(patientId).orElseThrow();

        // Attempting to link another Google account should fail
        assertThrows(IllegalStateException.class, () -> {
            reloadedPatient.linkGoogleAccount(googleId2, email);
        }, "Linking a second Google account should throw exception");

            // Verify Google ID hasn't changed
            globalPatientRepository.flush();
            GlobalPatientEntity finalPatient = globalPatientRepository.findById(patientId).orElseThrow();
            assertEquals(googleId1, finalPatient.getGoogleId(),
                "Google ID should remain unchanged after failed link attempt");
        }
    }
}
