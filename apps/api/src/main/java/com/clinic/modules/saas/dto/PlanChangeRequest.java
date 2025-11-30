package com.clinic.modules.saas.dto;

import com.clinic.modules.saas.model.PlanTier;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for plan upgrade or downgrade operations.
 */
public class PlanChangeRequest {

    @NotNull(message = "Target plan tier is required")
    private PlanTier targetTier;

    private String billingCycle; // Optional, defaults to current cycle

    private String reason; // Optional, used for manual overrides and audit logging

    public PlanChangeRequest() {
    }

    public PlanChangeRequest(PlanTier targetTier, String billingCycle) {
        this.targetTier = targetTier;
        this.billingCycle = billingCycle;
    }

    public PlanChangeRequest(PlanTier targetTier, String billingCycle, String reason) {
        this.targetTier = targetTier;
        this.billingCycle = billingCycle;
        this.reason = reason;
    }

    public PlanTier getTargetTier() {
        return targetTier;
    }

    public void setTargetTier(PlanTier targetTier) {
        this.targetTier = targetTier;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
