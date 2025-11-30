package com.clinic.modules.saas.service;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing billing access control.
 * Provides methods to check tenant billing status and determine access permissions.
 * Implements caching to reduce database queries for billing status checks.
 */
@Service
public class BillingAccessControlService {

    private static final Logger logger = LoggerFactory.getLogger(BillingAccessControlService.class);

    private final TenantRepository tenantRepository;
    private final BillingAuditLogger auditLogger;

    public BillingAccessControlService(TenantRepository tenantRepository, BillingAuditLogger auditLogger) {
        this.tenantRepository = tenantRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * Check if a tenant has active billing.
     * A tenant has active billing if their billing status is ACTIVE.
     *
     * @param tenantId the tenant ID
     * @return true if the tenant has active billing, false otherwise
     */
    @Cacheable(value = "billingStatus", key = "#tenantId", unless = "#result == null")
    public boolean hasActiveBilling(Long tenantId) {
        logger.debug("Checking active billing status for tenant: {}", tenantId);
        
        BillingStatus status = getBillingStatus(tenantId);
        boolean isActive = status == BillingStatus.ACTIVE;
        
        logger.debug("Tenant {} has active billing: {}", tenantId, isActive);
        return isActive;
    }

    /**
     * Get the billing status for a tenant.
     * Returns the current billing status from the database.
     * Results are cached for 5 minutes to improve performance.
     *
     * @param tenantId the tenant ID
     * @return the billing status, or PENDING_PAYMENT if tenant not found
     */
    @Cacheable(value = "billingStatus", key = "'status_' + #tenantId", unless = "#result == null")
    public BillingStatus getBillingStatus(Long tenantId) {
        logger.debug("Fetching billing status for tenant: {}", tenantId);
        
        return tenantRepository.findById(tenantId)
                .map(tenant -> {
                    BillingStatus status = tenant.getBillingStatus();
                    logger.debug("Tenant {} billing status: {}", tenantId, status);
                    return status;
                })
                .orElseGet(() -> {
                    logger.warn("Tenant not found: {}, returning PENDING_PAYMENT", tenantId);
                    return BillingStatus.PENDING_PAYMENT;
                });
    }

    /**
     * Check if a user can access the tenant admin panel.
     * Access is granted only if the tenant has ACTIVE billing status.
     * Other statuses (PENDING_PAYMENT, PAST_DUE, CANCELED) deny access.
     *
     * @param userId the user ID (for logging purposes)
     * @param tenantId the tenant ID
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessTenantAdmin(Long userId, Long tenantId) {
        logger.debug("Checking tenant admin access for user {} on tenant {}", userId, tenantId);
        
        boolean hasAccess = hasActiveBilling(tenantId);
        
        if (!hasAccess) {
            BillingStatus status = getBillingStatus(tenantId);
            logger.info("Access denied for user {} on tenant {} due to billing status: {}", 
                userId, tenantId, status);
        }
        
        return hasAccess;
    }

    /**
     * Manually override the billing status for a tenant.
     * This is a privileged operation that should only be performed by SaaS managers.
     * All overrides are logged for audit purposes.
     *
     * @param tenantId the tenant ID
     * @param newStatus the new billing status
     * @param managerId the SaaS manager ID performing the override
     * @param reason the reason for the override
     * @throws NotFoundException if tenant not found
     */
    @Transactional
    @CacheEvict(value = "billingStatus", allEntries = true)
    public void overrideBillingStatus(Long tenantId, BillingStatus newStatus, Long managerId, String reason) {
        logger.warn("Manual billing status override requested for tenant: {} by manager: {}", tenantId, managerId);
        
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    logger.error("Tenant not found for billing override: {}", tenantId);
                    return new NotFoundException("Tenant not found: " + tenantId);
                });

        BillingStatus oldStatus = tenant.getBillingStatus();
        
        if (oldStatus == newStatus) {
            logger.info("Billing status already set to {} for tenant: {}, no change needed", newStatus, tenantId);
            return;
        }

        tenant.setBillingStatus(newStatus);
        tenantRepository.save(tenant);

        logger.warn("Billing status manually overridden for tenant: {} from {} to {} by manager: {}. Reason: {}", 
            tenantId, oldStatus, newStatus, managerId, reason);
        
        auditLogger.logManualOverride(tenantId, managerId, oldStatus.name(), newStatus.name(), reason);
        auditLogger.logBillingStatusChange(tenantId, oldStatus.name(), newStatus.name(), 
            "Manual override by manager " + managerId + ": " + reason);
    }
}
