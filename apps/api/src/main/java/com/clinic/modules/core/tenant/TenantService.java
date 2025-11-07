package com.clinic.modules.core.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public Optional<TenantEntity> findActiveBySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            return Optional.empty();
        }
        return tenantRepository.findBySlugIgnoreCase(slug.trim())
                .filter(tenant -> tenant.getStatus() == TenantStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Optional<TenantEntity> findActiveByDomain(String domain) {
        if (!StringUtils.hasText(domain)) {
            return Optional.empty();
        }
        return tenantRepository.findByCustomDomainIgnoreCase(domain.trim())
                .filter(tenant -> tenant.getStatus() == TenantStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public TenantEntity requireActiveTenantBySlug(String slug) {
        return findActiveBySlug(slug)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found for slug: " + slug));
    }

    @Transactional(readOnly = true)
    public TenantEntity requireTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .filter(tenant -> tenant.getStatus() == TenantStatus.ACTIVE)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found for id: " + tenantId));
    }
}
