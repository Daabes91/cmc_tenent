package com.clinic.modules.publicapi.dto;

import jakarta.validation.constraints.NotBlank;

public record AvailabilityRequest(
        @NotBlank String serviceSlug,
        Long doctorId,
        String date
) {
}
