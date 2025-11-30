package com.clinic.modules.admin.controller;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.saas.service.BillingAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for tenant billing status operations.
 * Provides endpoints for tenant admin to check their billing status.
 */
@RestController
@RequestMapping("/admin/billing")
public class BillingStatusController {

    private static final Logger logger = LoggerFactory.getLogger(BillingStatusController.class);

    private final BillingAccessControlService billingAccessControlService;
    private final TenantContextHolder tenantContextHolder;

    public BillingStatusController(
            BillingAccessControlService billingAccessControlService,
            TenantContextHolder tenantContextHolder) {
        this.billingAccessControlService = billingAccessControlService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get the current billing status for the authenticated tenant.
     *
     * @return billing status response with status and isActive flag
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBillingStatus() {
        Long tenantId = tenantContextHolder.requireTenantId();

        logger.debug("Fetching billing status for tenant: {}", tenantId);
        
        BillingStatus status = billingAccessControlService.getBillingStatus(tenantId);
        boolean isActive = status == BillingStatus.ACTIVE;

        Map<String, Object> response = new HashMap<>();
        response.put("status", status.name().toLowerCase());
        response.put("isActive", isActive);

        logger.debug("Billing status for tenant {}: {}", tenantId, status);
        
        return ResponseEntity.ok(response);
    }
}
