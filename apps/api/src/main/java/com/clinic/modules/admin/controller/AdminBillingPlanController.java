package com.clinic.modules.admin.controller;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.saas.dto.CancelPlanRequest;
import com.clinic.modules.saas.dto.CancellationResponse;
import com.clinic.modules.saas.dto.PlanChangeRequest;
import com.clinic.modules.saas.dto.PlanChangeResponse;
import com.clinic.modules.saas.dto.PaymentMethodUpdateResponse;
import com.clinic.modules.saas.dto.TenantPlanResponse;
import com.clinic.modules.saas.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes plan management endpoints for tenant admins (clinic owners).
 */
@RestController("adminBillingPlanController")
@RequestMapping("/admin/billing/plan")
public class AdminBillingPlanController {

    private final SubscriptionService subscriptionService;
    private final TenantContextHolder tenantContextHolder;

    public AdminBillingPlanController(
            SubscriptionService subscriptionService,
            TenantContextHolder tenantContextHolder
    ) {
        this.subscriptionService = subscriptionService;
        this.tenantContextHolder = tenantContextHolder;
    }

    @GetMapping
    public ResponseEntity<TenantPlanResponse> getPlan() {
        Long tenantId = tenantContextHolder.requireTenantId();
        return ResponseEntity.ok(subscriptionService.getTenantPlan(tenantId));
    }

    @PostMapping("/upgrade")
    public ResponseEntity<PlanChangeResponse> upgradePlan(@Valid @RequestBody PlanChangeRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        PlanChangeResponse response = subscriptionService.upgradePlan(
                tenantId,
                request.getTargetTier(),
                request.getBillingCycle()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment-method")
    public ResponseEntity<PaymentMethodUpdateResponse> updatePaymentMethod() {
        Long tenantId = tenantContextHolder.requireTenantId();
        PaymentMethodUpdateResponse response = subscriptionService.createPaymentUpdateSession(tenantId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<CancellationResponse> cancelPlan(@Valid @RequestBody(required = false) CancelPlanRequest request) {
        Long tenantId = tenantContextHolder.requireTenantId();
        boolean immediate = request != null && request.isImmediate();
        String reason = request != null ? request.getReason() : null;
        CancellationResponse response = subscriptionService.cancelSubscription(tenantId, immediate, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resume")
    public ResponseEntity<Void> resumePlan() {
        Long tenantId = tenantContextHolder.requireTenantId();
        subscriptionService.resumeSubscription(tenantId);
        return ResponseEntity.noContent().build();
    }
}
