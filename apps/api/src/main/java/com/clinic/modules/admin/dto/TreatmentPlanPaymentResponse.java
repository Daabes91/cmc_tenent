package com.clinic.modules.admin.dto;

import com.clinic.modules.core.treatment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for treatment plan payment details.
 */
public record TreatmentPlanPaymentResponse(
        Long id,
        Long treatmentPlanId,
        BigDecimal amount,
        String currency,
        BigDecimal convertedAmount,
        String convertedCurrency,
        PaymentMethod paymentMethod,
        String transactionReference,
        Instant paymentDate,
        String notes,
        Long recordedByStaffId,
        String recordedByStaffName,
        Instant createdAt
) {
}