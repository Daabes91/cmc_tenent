package com.clinic.modules.core.patient;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service for managing global patient records.
 * Global patients represent a patient's identity across all tenants.
 * This service handles authentication and patient identity management.
 */
@Service
public class GlobalPatientService {

    private final GlobalPatientRepository globalPatientRepository;
    private final PasswordEncoder passwordEncoder;

    public GlobalPatientService(
            GlobalPatientRepository globalPatientRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.globalPatientRepository = globalPatientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Find an existing global patient by email, or create a new one if not found.
     * This method is used during patient registration to ensure a single global identity.
     *
     * @param email    the patient's email address (globally unique)
     * @param phone    the patient's phone number
     * @param dob      the patient's date of birth
     * @param password the patient's plain text password (will be hashed)
     * @return the existing or newly created global patient entity
     */
    @Transactional
    public GlobalPatientEntity findOrCreateGlobalPatient(
            String email,
            String phone,
            LocalDate dob,
            String password
    ) {
        // Check if global patient already exists
        Optional<GlobalPatientEntity> existing = globalPatientRepository.findByEmail(email);
        
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new global patient
        String hashedPassword = passwordEncoder.encode(password);
        GlobalPatientEntity globalPatient = new GlobalPatientEntity(
                email,
                phone,
                hashedPassword,
                dob
        );

        return globalPatientRepository.save(globalPatient);
    }

    /**
     * Find a global patient by email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the global patient if found
     */
    @Transactional(readOnly = true)
    public Optional<GlobalPatientEntity> findByEmail(String email) {
        return globalPatientRepository.findByEmail(email);
    }

    /**
     * Validate a password against a global patient's stored password hash.
     * This method is used during authentication to verify patient credentials.
     *
     * @param globalPatient the global patient entity
     * @param password      the plain text password to validate
     * @return true if the password matches, false otherwise
     */
    public boolean validatePassword(GlobalPatientEntity globalPatient, String password) {
        if (globalPatient == null || password == null) {
            return false;
        }
        
        return passwordEncoder.matches(password, globalPatient.getPasswordHash());
    }
}
