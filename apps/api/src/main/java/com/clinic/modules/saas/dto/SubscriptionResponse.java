package com.clinic.modules.saas.dto;

import com.clinic.modules.saas.json.LocalDateTimeToEpochMillisSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime currentPeriodStart,
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime currentPeriodEnd,
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime createdAt,
        @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
        LocalDateTime updatedAt
) {
}
