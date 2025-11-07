package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record DoctorAvailabilitySlotsRequest(
        @NotBlank String serviceSlug,
        String date
) {
}
