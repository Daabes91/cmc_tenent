package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AppointmentResponse(
        Long id,
        String patientName,
        String doctorName,
        String serviceName,
        OffsetDateTime scheduledAt,
        String status,
        String bookingMode,
        Long treatmentPlanId,
        Integer followUpVisitNumber,
        boolean paymentCollected,
        Boolean patientAttended,
        Integer slotDurationMinutes,
        BigDecimal paymentAmount,
        String paymentMethod,
        String paymentCurrency
) {
}
