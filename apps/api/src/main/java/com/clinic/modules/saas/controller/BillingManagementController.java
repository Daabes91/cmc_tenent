package com.clinic.modules.saas.controller;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.saas.dto.BillingStatusOverrideRequest;
import com.clinic.modules.saas.service.BillingAccessControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for SaaS manager billing management operations.
 * Provides endpoints for manual billing status overrides.
 */
@RestController
@RequestMapping("/saas/billing")
@Tag(name = "SaaS Billing Management", description = "Endpoints for managing tenant billing status")
@SecurityRequirement(name = "bearerAuth")
public class BillingManagementController {

    private static final Logger logger = LoggerFactory.getLogger(BillingManagementController.class);

    private final BillingAccessControlService billingAccessControlService;

    public BillingManagementController(BillingAccessControlService billingAccessControlService) {
        this.billingAccessControlService = billingAccessControlService;
    }

    /**
     * Manually override the billing status for a tenant.
     * This is a privileged operation restricted to SaaS managers.
     * All overrides are logged for audit purposes.
     *
     * @param request the override request containing tenant ID, new status, and reason
     * @param authentication the authenticated SaaS manager
     * @return success response
     */
    @PostMapping("/override-status")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Override tenant billing status",
            description = "Manually override the billing status for a tenant. This operation is logged for audit purposes."
    )
    public ResponseEntity<Map<String, String>> overrideBillingStatus(
            @Valid @RequestBody BillingStatusOverrideRequest request,
            @AuthenticationPrincipal Authentication authentication) {

        Long managerId = getManagerIdFromAuthentication(authentication);
        logger.info("Billing status override request received for tenant: {} by manager: {}", 
            request.getTenantId(), managerId);

        // Validate billing status
        BillingStatus newStatus;
        try {
            newStatus = BillingStatus.valueOf(request.getBillingStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid billing status provided: {}", request.getBillingStatus());
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "INVALID_BILLING_STATUS",
                            "message", "Invalid billing status. Valid values: PENDING_PAYMENT, ACTIVE, PAST_DUE, CANCELED"
                    ));
        }

        // Perform override
        billingAccessControlService.overrideBillingStatus(
                request.getTenantId(),
                newStatus,
                managerId,
                request.getReason()
        );

        logger.info("Billing status override completed for tenant: {}", request.getTenantId());

        return ResponseEntity.ok(
                Map.of("message", "Billing status updated successfully")
        );
    }

    /**
     * Get the current billing status for a tenant.
     *
     * @param tenantId the tenant ID
     * @return the current billing status
     */
    @GetMapping("/status/{tenantId}")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Get tenant billing status",
            description = "Retrieve the current billing status for a tenant"
    )
    public ResponseEntity<Map<String, String>> getBillingStatus(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal Authentication authentication) {

        Long managerId = getManagerIdFromAuthentication(authentication);
        logger.debug("Billing status request for tenant: {} by manager: {}", tenantId, managerId);

        BillingStatus status = billingAccessControlService.getBillingStatus(tenantId);
        
        return ResponseEntity.ok(
                Map.of(
                        "tenantId", tenantId.toString(),
                        "billingStatus", status.name()
                )
        );
    }

    /**
     * Extract manager ID from authentication.
     * For now, returns a default value since we don't have the full SaaS manager authentication implemented.
     *
     * @param authentication the authentication object
     * @return the manager ID
     */
    private Long getManagerIdFromAuthentication(Authentication authentication) {
        // TODO: Extract actual manager ID from authentication once SaaS manager authentication is fully implemented
        // For now, return a default value for logging purposes
        return 1L;
    }
}
