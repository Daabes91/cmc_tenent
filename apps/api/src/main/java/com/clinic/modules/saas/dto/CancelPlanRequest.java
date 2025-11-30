package com.clinic.modules.saas.dto;

/**
 * Request DTO for subscription cancellation.
 */
public class CancelPlanRequest {

    private boolean immediate = false;
    private String reason; // Optional cancellation reason

    public CancelPlanRequest() {
    }

    public CancelPlanRequest(boolean immediate, String reason) {
        this.immediate = immediate;
        this.reason = reason;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
