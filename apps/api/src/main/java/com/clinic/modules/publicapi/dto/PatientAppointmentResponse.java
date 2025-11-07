package com.clinic.modules.publicapi.dto;

import java.time.Instant;

public record PatientAppointmentResponse(
        Long id,
        String service,
        DoctorInfo doctor,
        String scheduledAt,
        String status,
        String bookingMode,
        String notes,
        String createdAt
) {
    public record DoctorInfo(
            Long id,
            String name,
            String specialization
    ) {}
}
