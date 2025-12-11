package com.clinic.modules.saas.repository;

import com.clinic.modules.saas.model.PayPalConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PayPal configuration management.
 * Since there should only be one PayPal configuration per system,
 * we provide a method to find the first (and only) configuration.
 */
@Repository
public interface PayPalConfigRepository extends JpaRepository<PayPalConfigEntity, Long> {

    /**
     * Find the first PayPal configuration.
     * Since there should only be one configuration, this returns the active config.
     *
     * @return Optional containing the PayPal configuration if it exists
     */
    Optional<PayPalConfigEntity> findFirstByOrderByIdAsc();

}
