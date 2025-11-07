package com.clinic.modules.admin.dto;

import com.clinic.modules.core.treatment.FollowUpCadence;
import com.clinic.modules.core.treatment.TreatmentPlanStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for treatment plan details with dynamic calculations.
 * Includes both original prices (in original currency) and converted prices (in clinic currency).
 */
public record TreatmentPlanResponse(
        Long id,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        Long treatmentTypeId,
        String treatmentTypeName,
        BigDecimal totalPrice,           // Original price
        String currency,                 // Original currency
        Integer plannedFollowups,
        FollowUpCadence followUpCadence,
        Integer completedVisits,
        TreatmentPlanStatus status,
        String notes,
        BigDecimal discountAmount,
        String discountReason,
        Instant createdAt,
        Instant updatedAt,
        Instant startedAt,
        Instant completedAt,

        // Dynamic calculations (original currency)
        BigDecimal totalPaid,
        BigDecimal remainingBalance,
        Integer remainingVisits,
        BigDecimal expectedPaymentPerVisit,
        BigDecimal totalMaterialsCost,
        BigDecimal netRevenue,

        // Converted prices (in clinic currency)
        BigDecimal convertedTotalPrice,
        String convertedCurrency,
        BigDecimal convertedTotalPaid,
        BigDecimal convertedRemainingBalance,
        BigDecimal convertedExpectedPaymentPerVisit,
        BigDecimal convertedTotalMaterialsCost,
        BigDecimal convertedNetRevenue,

        // Related data
        List<FollowUpVisitResponse> followUpVisits,
        List<ScheduledFollowUpResponse> scheduledFollowUps
) {
}
