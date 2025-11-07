package com.clinic.modules.admin.dto;

import com.clinic.modules.core.treatment.PaymentMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AppointmentAdminUpsertRequest(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        @NotNull Long serviceId,
        @NotBlank String scheduledAt,
        @NotBlank String bookingMode,
        String notes,
        Long treatmentPlanId,
        Integer followUpVisitNumber,
        Boolean paymentCollected,
        Boolean patientAttended,
        BigDecimal paymentAmount,
        PaymentMethod paymentMethod,
        String paymentCurrency,
        String paymentDate,
        String paymentReference,
        String paymentNotes,
        @Min(5) @Max(240) Integer slotDurationMinutes
) {
}
