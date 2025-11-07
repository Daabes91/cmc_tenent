package com.clinic.modules.admin.dto;

import java.time.Instant;

/**
 * Represents a scheduled follow-up appointment associated with a treatment plan.
 */
public record ScheduledFollowUpResponse(
        Long appointmentId,
        Integer visitNumber,
        Instant scheduledAt,
        String status,
        String bookingMode,
        boolean paymentCollected,
        Boolean patientAttended
) {
}
