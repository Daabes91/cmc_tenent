package com.clinic.modules.saas.exception;

import com.clinic.modules.saas.model.PlanTier;

/**
 * Exception thrown when an invalid plan tier is specified.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidPlanTierException extends RuntimeException {
    private final String requestedTier;
    private final PlanTier[] availableTiers;

    public InvalidPlanTierException(String requestedTier, PlanTier[] availableTiers) {
        super("Invalid plan tier: " + requestedTier + ". Available tiers: " + formatAvailableTiers(availableTiers));
        this.requestedTier = requestedTier;
        this.availableTiers = availableTiers;
    }

    private static String formatAvailableTiers(PlanTier[] tiers) {
        if (tiers == null || tiers.length == 0) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiers.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(tiers[i].name());
        }
        return sb.toString();
    }

    public String getRequestedTier() {
        return requestedTier;
    }

    public PlanTier[] getAvailableTiers() {
        return availableTiers;
    }
}
