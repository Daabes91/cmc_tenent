package com.clinic.modules.saas.dto;

import com.clinic.modules.saas.model.PlanTier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload describing a tenant's active subscription plan.
 * Contains all plan details including tier, pricing, renewal information, and payment method.
 */
public record TenantPlanResponse(
        Long tenantId,
        PlanTier planTier,
        String planTierName,
        BigDecimal price,
        String currency,
        String billingCycle,
        LocalDateTime renewalDate,
        String paymentMethodMask,
        String paymentMethodType,
        String status,
        LocalDateTime cancellationDate,
        LocalDateTime cancellationEffectiveDate,
        PlanTier pendingPlanTier,
        LocalDateTime pendingPlanEffectiveDate,
        List<String> features,
        String paypalSubscriptionId
) {
}
