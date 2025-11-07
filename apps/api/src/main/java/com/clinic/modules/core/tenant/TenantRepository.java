package com.clinic.modules.core.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    Optional<TenantEntity> findBySlugIgnoreCase(String slug);

    Optional<TenantEntity> findByCustomDomainIgnoreCase(String domain);
}
