package com.clinic.modules.saas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a tenant attempts to exceed their subscription plan limits.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class PlanLimitExceededException extends RuntimeException {
    
    private final String limitType;
    private final int currentCount;
    private final int maxAllowed;
    
    public PlanLimitExceededException(String message) {
        super(message);
        this.limitType = "UNKNOWN";
        this.currentCount = -1;
        this.maxAllowed = -1;
    }

    public PlanLimitExceededException(String limitType, int currentCount, int maxAllowed) {
        super(String.format("Plan limit exceeded: %s. Current: %d, Maximum allowed: %d. Please upgrade your plan.",
                limitType, currentCount, maxAllowed));
        this.limitType = limitType;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
    }
    
    public String getLimitType() {
        return limitType;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public int getMaxAllowed() {
        return maxAllowed;
    }
}
