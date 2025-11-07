package com.clinic.modules.core.settings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicSettingsRepository extends JpaRepository<ClinicSettingsEntity, Long> {
    Optional<ClinicSettingsEntity> findFirstByOrderByIdAsc();
    Optional<ClinicSettingsEntity> findByTenantId(Long tenantId);
}
