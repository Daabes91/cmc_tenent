package com.clinic.modules.admin.dto;

import java.math.BigDecimal;

public record AppointmentAdminDetailResponse(
        Long id,
        Reference patient,
        Reference doctor,
        Reference service,
        String status,
        String bookingMode,
        String scheduledAt,
        String createdAt,
        String notes,
        Long treatmentPlanId,
        Integer followUpVisitNumber,
        boolean paymentCollected,
        Boolean patientAttended,
        Integer slotDurationMinutes,
        BigDecimal paymentAmount,
        String paymentMethod,
        String paymentCurrency,
        String paymentDate,
        String paymentReference,
        String paymentNotes
) {
    public record Reference(Long id, String name) {
    }
}
