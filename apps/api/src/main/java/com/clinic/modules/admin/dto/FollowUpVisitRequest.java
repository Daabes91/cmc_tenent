package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

/**
 * Request DTO for recording a follow-up visit.
 */
public record FollowUpVisitRequest(
        Long appointmentId,

        @NotNull(message = "Visit date is required")
        Instant visitDate,

        String notes,

        String performedProcedures,

        List<PaymentRequest> payments,

        List<MaterialUsageRequest> materials
) {
}
