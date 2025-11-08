package com.clinic.modules.admin.dto;

import java.time.Instant;

/**
 * Response DTO for team members on call
 */
public record TeamOnCallResponse(
        Long id,
        String name,
        String specialty,
        String status,
        Instant nextSlot
) {
}
