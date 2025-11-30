package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.exception.UnauthorizedException;
import com.clinic.modules.saas.service.SubscriptionService;
import com.clinic.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for subscription plan management operations.
 * Provides endpoints for viewing, upgrading, downgrading, and cancelling subscription plans.
 * All endpoints require SaaS manager authentication.
 */
@RestController
@RequestMapping({
    "/saas/tenants/{tenantId}/plan",
    "/api/saas/tenants/{tenantId}/plan"
})
@Tag(name = "Subscription Plan Management", description = "Endpoints for managing tenant subscription plans")
@SecurityRequirement(name = "bearerAuth")
public class BillingPlanController {

    private static final Logger logger = LoggerFactory.getLogger(BillingPlanController.class);

    private final SubscriptionService subscriptionService;

    public BillingPlanController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Get the current subscription plan details for a tenant.
     * Returns plan tier, pricing, renewal date, payment method, and status.
     *
     * @param tenantId the tenant ID
     * @param principal the authenticated SaaS manager
     * @return TenantPlanResponse with complete plan details
     */
    @GetMapping
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Get tenant plan details",
            description = "Retrieve the current subscription plan details for a tenant including tier, pricing, renewal date, and payment method"
    )
    public ResponseEntity<TenantPlanResponse> getPlanDetails(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        JwtPrincipal manager = requirePrincipal(principal);
        logger.info("Fetching plan details for tenant: {} by manager: {}", tenantId, manager.subject());

        TenantPlanResponse response = subscriptionService.getPlanDetails(tenantId);

        logger.info("Successfully fetched plan details for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Upgrade a tenant's subscription plan to a higher tier.
     * Generates PayPal approval URL for the upgrade.
     *
     * @param tenantId the tenant ID
     * @param request the plan change request with target tier
     * @param principal the authenticated SaaS manager
     * @return PlanChangeResponse with PayPal approval URL
     */
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Upgrade tenant plan",
            description = "Upgrade a tenant's subscription plan to a higher tier. Returns PayPal approval URL for completing the upgrade."
    )
    public ResponseEntity<PlanChangeResponse> upgradePlan(
            @PathVariable Long tenantId,
            @Valid @RequestBody PlanChangeRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        JwtPrincipal manager = requirePrincipal(principal);
        logger.info("Plan upgrade request for tenant: {} to tier: {} by manager: {}", 
            tenantId, request.getTargetTier(), manager.subject());

        PlanChangeResponse response = subscriptionService.upgradePlan(
                tenantId,
                request.getTargetTier(),
                request.getBillingCycle()
        );

        logger.info("Successfully initiated plan upgrade for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Downgrade a tenant's subscription plan to a lower tier.
     * Schedules the downgrade for the next billing cycle.
     *
     * @param tenantId the tenant ID
     * @param request the plan change request with target tier
     * @param principal the authenticated SaaS manager
     * @return PlanChangeResponse with effective date
     */
    @PostMapping("/downgrade")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Downgrade tenant plan",
            description = "Downgrade a tenant's subscription plan to a lower tier. The change will take effect at the next billing cycle."
    )
    public ResponseEntity<PlanChangeResponse> downgradePlan(
            @PathVariable Long tenantId,
            @Valid @RequestBody PlanChangeRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        JwtPrincipal manager = requirePrincipal(principal);
        logger.info("Plan downgrade request for tenant: {} to tier: {} by manager: {}", 
            tenantId, request.getTargetTier(), manager.subject());

        PlanChangeResponse response = subscriptionService.downgradePlan(
                tenantId,
                request.getTargetTier(),
                request.getBillingCycle()
        );

        logger.info("Successfully scheduled plan downgrade for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a tenant's subscription.
     * Can be immediate or scheduled for end of billing period.
     *
     * @param tenantId the tenant ID
     * @param request the cancellation request with immediate flag and reason
     * @param principal the authenticated SaaS manager
     * @return CancellationResponse with effective date
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Cancel tenant subscription",
            description = "Cancel a tenant's subscription. Can be immediate or scheduled for the end of the current billing period."
    )
    public ResponseEntity<CancellationResponse> cancelPlan(
            @PathVariable Long tenantId,
            @RequestBody CancelPlanRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        JwtPrincipal manager = requirePrincipal(principal);
        logger.info("Subscription cancellation request for tenant: {}, immediate: {} by manager: {}", 
            tenantId, request.isImmediate(), manager.subject());

        CancellationResponse response = subscriptionService.cancelSubscription(
                tenantId,
                request.isImmediate(),
                request.getReason()
        );

        logger.info("Successfully cancelled subscription for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Resume a cancelled subscription.
     * Reactivates a subscription that was previously cancelled.
     *
     * @param tenantId the tenant ID
     * @param principal the authenticated SaaS manager
     * @return success response
     */
    @PostMapping("/resume")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Resume cancelled subscription",
            description = "Reactivate a subscription that was previously cancelled"
    )
    public ResponseEntity<Void> resumePlan(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        JwtPrincipal manager = requirePrincipal(principal);
        logger.info("Subscription resume request for tenant: {} by manager: {}", 
            tenantId, manager.subject());

        subscriptionService.resumeSubscription(tenantId);

        logger.info("Successfully resumed subscription for tenant: {}", tenantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Initiate payment method update for a tenant.
     * Generates PayPal billing portal URL for updating payment method.
     *
     * @param tenantId the tenant ID
     * @param principal the authenticated SaaS manager
     * @return PaymentMethodUpdateResponse with portal URL
     */
    @PostMapping("/payment-method")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Update payment method",
            description = "Generate PayPal billing portal URL for updating the tenant's payment method"
    )
    public ResponseEntity<PaymentMethodUpdateResponse> initiatePaymentUpdate(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        JwtPrincipal manager = requirePrincipal(principal);
        logger.info("Payment method update request for tenant: {} by manager: {}", 
            tenantId, manager.subject());

        PaymentMethodUpdateResponse response = subscriptionService.createPaymentUpdateSession(tenantId);

        logger.info("Successfully generated payment update URL for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Manually override a tenant's plan tier.
     * This is a privileged operation for handling special cases and support issues.
     * All overrides are logged with manager identity and reason.
     *
     * @param tenantId the tenant ID
     * @param request the plan change request with target tier and reason
     * @param principal the authenticated SaaS manager
     * @return success response
     */
    @PostMapping("/override")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Manually override tenant plan",
            description = "Manually override a tenant's plan tier. This operation is logged for audit purposes and should only be used for special cases."
    )
    public ResponseEntity<Void> overridePlan(
            @PathVariable Long tenantId,
            @Valid @RequestBody PlanChangeRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        JwtPrincipal manager = requirePrincipal(principal);
        logger.warn("Manual plan override request for tenant: {} to tier: {} by manager: {}", 
            tenantId, request.getTargetTier(), manager.subject());

        Long managerId = resolveManagerId(manager);

        String reason = request.getReason() != null ? request.getReason() : "Manual override by SaaS manager";

        subscriptionService.manualPlanOverride(
                tenantId,
                request.getTargetTier(),
                managerId,
                reason
        );

        logger.info("Successfully overridden plan for tenant: {}", tenantId);
        return ResponseEntity.noContent().build();
    }

    private JwtPrincipal requirePrincipal(JwtPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication is required to manage tenant plans.");
        }
        return principal;
    }

    private Long resolveManagerId(JwtPrincipal principal) {
        try {
            return Long.parseLong(principal.subject());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
