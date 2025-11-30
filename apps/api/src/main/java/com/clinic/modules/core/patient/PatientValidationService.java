package com.clinic.modules.core.patient;

import org.springframework.stereotype.Service;

/**
 * Service for validating patient data and enforcing business rules.
 * This provides application-layer validation in addition to database constraints.
 */
@Service
public class PatientValidationService {

    private final PatientRepository patientRepository;
    private final GlobalPatientRepository globalPatientRepository;

    public PatientValidationService(PatientRepository patientRepository, 
                                   GlobalPatientRepository globalPatientRepository) {
        this.patientRepository = patientRepository;
        this.globalPatientRepository = globalPatientRepository;
    }

    /**
     * Validate that email is unique within the tenant.
     * 
     * @param email The email to validate
     * @param tenantId The tenant ID
     * @param excludePatientId Optional patient ID to exclude from the check (for updates)
     * @throws DuplicateEmailException if email already exists in the tenant
     */
    public void validateEmailUniquenessInTenant(String email, Long tenantId, Long excludePatientId) {
        if (email == null || email.trim().isEmpty()) {
            return; // Email is optional
        }

        String normalizedEmail = normalizeEmail(email);
        
        boolean exists = patientRepository.findByTenantIdAndEmail(tenantId, normalizedEmail)
            .filter(patient -> !patient.getId().equals(excludePatientId))
            .isPresent();

        if (exists) {
            throw new DuplicateEmailException(
                String.format("Email '%s' is already in use by another patient in this clinic", normalizedEmail)
            );
        }
    }

    /**
     * Validate that Google ID is unique within the tenant.
     * 
     * @param googleId The Google ID to validate
     * @param tenantId The tenant ID
     * @param excludePatientId Optional patient ID to exclude from the check (for updates)
     * @throws DuplicateGoogleIdException if Google ID already exists in the tenant
     */
    public void validateGoogleIdUniquenessInTenant(String googleId, Long tenantId, Long excludePatientId) {
        if (googleId == null || googleId.trim().isEmpty()) {
            return;
        }

        boolean exists = patientRepository.findByTenantIdAndGoogleId(tenantId, googleId)
            .filter(patient -> !patient.getId().equals(excludePatientId))
            .isPresent();

        if (exists) {
            throw new DuplicateGoogleIdException(
                "This Google account is already linked to another patient in this clinic"
            );
        }
    }

    /**
     * Validate that a Google ID is not being changed after it's been set.
     * Google IDs should be immutable once assigned.
     * 
     * @param existingGoogleId The current Google ID (may be null)
     * @param newGoogleId The new Google ID being set
     * @throws GoogleIdImmutableException if attempting to change an existing Google ID
     */
    public void validateGoogleIdImmutability(String existingGoogleId, String newGoogleId) {
        // If there's no existing Google ID, any new value is allowed
        if (existingGoogleId == null || existingGoogleId.trim().isEmpty()) {
            return;
        }

        // If there is an existing Google ID, it cannot be changed
        if (newGoogleId != null && !existingGoogleId.equals(newGoogleId)) {
            throw new GoogleIdImmutableException(
                "Google ID cannot be changed once set. Existing: " + existingGoogleId
            );
        }
    }

    /**
     * Validate that email is unique globally (across all tenants).
     * This is used for global_patients table.
     * 
     * @param email The email to validate
     * @param excludeGlobalPatientId Optional global patient ID to exclude from the check (for updates)
     * @throws DuplicateEmailException if email already exists globally
     */
    public void validateEmailUniquenessGlobally(String email, Long excludeGlobalPatientId) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required for global patient");
        }

        String normalizedEmail = normalizeEmail(email);
        
        boolean exists = globalPatientRepository.findByEmail(normalizedEmail)
            .filter(patient -> !patient.getId().equals(excludeGlobalPatientId))
            .isPresent();

        if (exists) {
            throw new DuplicateEmailException(
                String.format("Email '%s' is already registered", normalizedEmail)
            );
        }
    }

    /**
     * Normalize email address for consistent comparison.
     * Converts to lowercase and trims whitespace.
     * 
     * @param email The email to normalize
     * @return Normalized email
     */
    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Exception thrown when attempting to create a patient with a duplicate email in the same tenant.
     */
    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when attempting to create a patient with a duplicate Google ID in the same tenant.
     */
    public static class DuplicateGoogleIdException extends RuntimeException {
        public DuplicateGoogleIdException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when attempting to change an immutable Google ID.
     */
    public static class GoogleIdImmutableException extends RuntimeException {
        public GoogleIdImmutableException(String message) {
            super(message);
        }
    }
}
