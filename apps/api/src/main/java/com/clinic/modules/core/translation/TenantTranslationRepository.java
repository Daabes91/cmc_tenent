package com.clinic.modules.core.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TenantTranslationRepository extends JpaRepository<TenantTranslationEntity, Long> {
    List<TenantTranslationEntity> findByTenantId(Long tenantId);

    List<TenantTranslationEntity> findByTenantIdAndLocale(Long tenantId, String locale);

    List<TenantTranslationEntity> findByTenantIdAndNamespace(Long tenantId, String namespace);

    List<TenantTranslationEntity> findByTenantIdAndNamespaceAndLocale(Long tenantId, String namespace, String locale);

    @Query("SELECT t FROM TenantTranslationEntity t WHERE t.tenant.id = :tenantId AND (:namespace IS NULL OR t.namespace = :namespace) AND (:locale IS NULL OR t.locale = :locale)")
    List<TenantTranslationEntity> findByTenantAndFilters(Long tenantId, String namespace, String locale);

    Optional<TenantTranslationEntity> findByTenantIdAndNamespaceAndTranslationKeyAndLocale(Long tenantId, String namespace, String translationKey, String locale);
}
