package com.clinic.modules.saas.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for subscription information
 */
public record SubscriptionResponse(
        Long id,
        Long tenantId,
        String provider,
        String paypalSubscriptionId,
        String status,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
