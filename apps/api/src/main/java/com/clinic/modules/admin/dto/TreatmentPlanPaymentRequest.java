package com.clinic.modules.admin.dto;

import com.clinic.modules.core.treatment.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request DTO for recording a direct payment against a treatment plan.
 */
public record TreatmentPlanPaymentRequest(
        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String transactionReference,

        @NotNull(message = "Payment date is required")
        Instant paymentDate,

        String notes
) {
}