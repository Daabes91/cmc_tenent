package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoctorAvailabilityRequest(
        @NotBlank String type,
        String dayOfWeek,
        String date,
        @NotBlank String startTime,
        @NotBlank String endTime
) {
}
