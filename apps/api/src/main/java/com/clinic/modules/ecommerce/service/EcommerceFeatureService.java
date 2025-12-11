package com.clinic.modules.ecommerce.service;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.ecommerce.exception.EcommerceFeatureDisabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing e-commerce feature flags and validation.
 * 
 * This service provides methods to check if e-commerce functionality is enabled
 * for specific tenants and enforces feature flag validation across the system.
 */
@Service
public class EcommerceFeatureService {

    private static final Logger log = LoggerFactory.getLogger(EcommerceFeatureService.class);

    private final TenantRepository tenantRepository;

    public EcommerceFeatureService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * Check if e-commerce is enabled for a tenant.
     * 
     * @param tenantId the tenant ID to check
     * @return true if e-commerce is enabled, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isEcommerceEnabled(Long tenantId) {
        log.debug("Checking e-commerce feature flag for tenant: {}", tenantId);
        
        TenantEntity tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant == null) {
            log.warn("Tenant not found when checking e-commerce feature flag: {}", tenantId);
            return false;
        }

        boolean enabled = tenant.isEcommerceEnabled();
        log.debug("E-commerce feature flag for tenant {}: {}", tenantId, enabled);
        return enabled;
    }

    /**
     * Validate that e-commerce is enabled for a tenant, throwing an exception if not.
     * 
     * @param tenantId the tenant ID to validate
     * @throws EcommerceFeatureDisabledException if e-commerce is not enabled
     */
    @Transactional(readOnly = true)
    public void validateEcommerceEnabled(Long tenantId) {
        log.debug("Validating e-commerce feature flag for tenant: {}", tenantId);
        
        if (!isEcommerceEnabled(tenantId)) {
            log.warn("E-commerce feature access denied for tenant: {}", tenantId);
            throw new EcommerceFeatureDisabledException(tenantId);
        }
        
        log.debug("E-commerce feature validation passed for tenant: {}", tenantId);
    }

    /**
     * Enable e-commerce for a tenant.
     * 
     * @param tenantId the tenant ID
     * @throws IllegalArgumentException if tenant not found
     */
    @Transactional
    public void enableEcommerce(Long tenantId) {
        log.info("Enabling e-commerce for tenant: {}", tenantId);
        
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        
        tenant.setEcommerceEnabled(true);
        tenantRepository.save(tenant);
        
        log.info("E-commerce enabled successfully for tenant: {}", tenantId);
    }

    /**
     * Disable e-commerce for a tenant.
     * 
     * @param tenantId the tenant ID
     * @throws IllegalArgumentException if tenant not found
     */
    @Transactional
    public void disableEcommerce(Long tenantId) {
        log.info("Disabling e-commerce for tenant: {}", tenantId);
        
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        
        tenant.setEcommerceEnabled(false);
        tenantRepository.save(tenant);
        
        log.info("E-commerce disabled successfully for tenant: {}", tenantId);
    }
}