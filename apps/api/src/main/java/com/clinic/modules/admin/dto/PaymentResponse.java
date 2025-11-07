package com.clinic.modules.admin.dto;

import com.clinic.modules.core.treatment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for payment details with currency conversion.
 */
public record PaymentResponse(
        Long id,
        BigDecimal amount,
        String currency,
        BigDecimal convertedAmount,
        String convertedCurrency,
        PaymentMethod paymentMethod,
        String transactionReference,
        Instant paymentDate,
        String notes,
        Instant createdAt
) {
}
