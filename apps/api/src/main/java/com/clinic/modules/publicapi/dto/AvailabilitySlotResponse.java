package com.clinic.modules.publicapi.dto;

public record AvailabilitySlotResponse(
        Long doctorId,
        String doctorName,
        String start,
        String end
) {
}
