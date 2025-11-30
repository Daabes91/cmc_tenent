package com.clinic.modules.admin.controller;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.saas.dto.TenantPlanResponse;
import com.clinic.modules.saas.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for tenant admin billing operations.
 * Provides read-only endpoints for tenant administrators to view their subscription plan details.
 * Tenant context is automatically extracted from JWT authentication.
 */
@RestController
@RequestMapping("/admin/billing")
@Tag(name = "Admin Billing", description = "Endpoints for tenant administrators to view their billing information")
@SecurityRequirement(name = "bearerAuth")
public class AdminBillingController {

    private static final Logger logger = LoggerFactory.getLogger(AdminBillingController.class);

    private final SubscriptionService subscriptionService;
    private final TenantContextHolder tenantContextHolder;

    public AdminBillingController(
            SubscriptionService subscriptionService,
            TenantContextHolder tenantContextHolder) {
        this.subscriptionService = subscriptionService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Get the current subscription plan details for the authenticated tenant.
     * Returns plan tier, pricing, renewal date, payment method, and status.
     * Results are cached for 5 minutes for improved performance.
     *
     * @return TenantPlanResponse with complete plan details
     */
    @GetMapping("/current-plan")
    @Operation(
            summary = "Get current plan details",
            description = "Retrieve the current subscription plan details for the authenticated tenant including tier, pricing, renewal date, and payment method. Results are cached for 5 minutes."
    )
    public ResponseEntity<TenantPlanResponse> getCurrentPlan() {
        // Extract tenant ID from JWT context
        Long tenantId = tenantContextHolder.requireTenantId();

        logger.info("Fetching current plan details for tenant: {}", tenantId);

        // Fetch plan details (cached for 5 minutes)
        TenantPlanResponse response = subscriptionService.getPlanDetails(tenantId);

        logger.info("Successfully fetched current plan details for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }
}
