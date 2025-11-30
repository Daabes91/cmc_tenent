package com.clinic.modules.core.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for managing global patient records.
 * Global patients represent a patient's identity across all tenants.
 */
public interface GlobalPatientRepository extends JpaRepository<GlobalPatientEntity, Long> {

    /**
     * Find a global patient by email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the global patient if found
     */
    Optional<GlobalPatientEntity> findByEmail(String email);

    /**
     * Find a global patient by external ID.
     *
     * @param externalId the external ID (e.g., "PAT-uuid") to search for
     * @return an Optional containing the global patient if found
     */
    Optional<GlobalPatientEntity> findByExternalId(String externalId);

    /**
     * Check if a global patient exists with the given email address.
     *
     * @param email the email address to check
     * @return true if a global patient with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find a global patient by Google ID.
     *
     * @param googleId the Google ID to search for
     * @return an Optional containing the global patient if found
     */
    Optional<GlobalPatientEntity> findByGoogleId(String googleId);

    /**
     * Attempt to update Google ID directly (for testing database trigger).
     * This method bypasses entity validation to test database-level constraints.
     * Should fail due to database trigger if Google ID is already set.
     *
     * @param id the patient ID
     * @param googleId the new Google ID
     */
    @Modifying
    @Query(value = "UPDATE global_patients SET google_id = :googleId WHERE id = :id", nativeQuery = true)
    void updateGoogleIdDirectly(@Param("id") Long id, @Param("googleId") String googleId);
}
