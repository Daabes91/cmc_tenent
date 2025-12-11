package com.clinic.modules.saas.dto;

import com.clinic.modules.saas.json.LocalDateTimeToEpochMillisSerializer;
import com.clinic.modules.saas.model.PlanTier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime renewalDate,
        String paymentMethodMask,
        String paymentMethodType,
        String status,
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime cancellationDate,
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime cancellationEffectiveDate,
        PlanTier pendingPlanTier,
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime pendingPlanEffectiveDate,
        List<String> features,
        String paypalSubscriptionId,
        int maxStaff,
        int maxDoctors,
        long staffUsed,
        long doctorsUsed
) {
}
