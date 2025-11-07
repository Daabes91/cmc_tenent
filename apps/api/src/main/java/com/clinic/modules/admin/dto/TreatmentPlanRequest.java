package com.clinic.modules.admin.dto;

import com.clinic.modules.core.treatment.FollowUpCadence;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new treatment plan.
 */
public record TreatmentPlanRequest(
        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotNull(message = "Doctor ID is required")
        Long doctorId,

        @NotNull(message = "Treatment type ID is required")
        Long treatmentTypeId,

        @NotNull(message = "Total price is required")
        @DecimalMin(value = "0.01", message = "Total price must be greater than 0")
        BigDecimal totalPrice,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters (e.g., JOD, USD)")
        String currency,

        @NotNull(message = "Planned follow-ups is required")
        @Min(value = 1, message = "Must have at least 1 planned follow-up")
        Integer plannedFollowups,

        @NotNull(message = "Follow-up cadence is required")
        FollowUpCadence followUpCadence,

        String notes
) {
}
