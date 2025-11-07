package com.clinic.modules.publicapi.dto;

import jakarta.validation.constraints.NotBlank;

public record BookingRequest(
        @NotBlank String serviceSlug,
        Long doctorId,
        @NotBlank String slot,
        @NotBlank String bookingMode,
        String notes
) {
}
