package com.clinic.modules.saas.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for subscription cancellation operations.
 * Contains effective date and confirmation message.
 */
public record CancellationResponse(
        LocalDateTime effectiveDate,
        String message,
        boolean immediate
) {
}
