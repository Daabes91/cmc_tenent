package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for follow-up visit details.
 */
public record FollowUpVisitResponse(
        Long id,
        Integer visitNumber,
        Long appointmentId,
        Instant visitDate,
        String notes,
        String performedProcedures,
        Instant createdAt,
        List<PaymentResponse> payments,
        List<MaterialUsageResponse> materials,
        BigDecimal totalPaymentsThisVisit,
        BigDecimal totalMaterialsCostThisVisit
) {
}
