package com.clinic.modules.admin.dto;

import com.clinic.modules.core.treatment.FollowUpCadence;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing treatment plan.
 */
public record TreatmentPlanUpdateRequest(
        @NotNull(message = "Total price is required")
        @DecimalMin(value = "0.01", message = "Total price must be greater than 0")
        BigDecimal totalPrice,

        @NotNull(message = "Planned follow-ups is required")
        @Min(value = 1, message = "Must have at least 1 planned follow-up")
        Integer plannedFollowups,

        @NotNull(message = "Follow-up cadence is required")
        FollowUpCadence followUpCadence,

        String notes
) {
}
