package com.clinic.modules.publicapi.dto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Response DTO for clinic timezone information.
 * Provides timezone details for frontend display and conversion.
 */
public record TimezoneResponse(
        String timezone,        // IANA timezone identifier (e.g., "Asia/Amman")
        String abbreviation,    // Timezone abbreviation (e.g., "EET", "EEST")
        String offset,          // Current UTC offset (e.g., "+03:00")
        String currentTime      // Current time in clinic timezone (ISO-8601)
) {
    /**
     * Factory method to create timezone response from ZoneId.
     *
     * @param zoneId The clinic timezone
     * @return TimezoneResponse with all fields populated
     */
    public static TimezoneResponse from(ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        return new TimezoneResponse(
                zoneId.getId(),
                now.getZone().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                now.getOffset().toString(),
                now.toString()
        );
    }
}
