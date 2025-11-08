package com.clinic.modules.saas.repository;

import com.clinic.modules.saas.model.SaasManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for SAAS Manager data access.
 * Provides methods to query and persist SAAS Manager entities.
 */
public interface SaasManagerRepository extends JpaRepository<SaasManager, Long> {

    /**
     * Find a SAAS Manager by email address (case-insensitive)
     *
     * @param email The email address to search for
     * @return Optional containing the SAAS Manager if found, empty otherwise
     */
    Optional<SaasManager> findByEmailIgnoreCase(String email);
}
