package com.clinic.modules.admin.dto;

public record DoctorAvailabilityResponse(
        Long id,
        String type,
        String dayOfWeek,
        String date,
        String startTime,
        String endTime,
        String createdAt,
        String updatedAt
) {
}
