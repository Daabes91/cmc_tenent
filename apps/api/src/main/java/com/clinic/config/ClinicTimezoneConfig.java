package com.clinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * Configuration for clinic timezone settings.
 * This allows the system to work in different geographical locations.
 *
 * Examples:
 * - Jordan: Asia/Amman (UTC+3)
 * - UAE/Dubai: Asia/Dubai (UTC+4)
 * - Saudi Arabia: Asia/Riyadh (UTC+3)
 */
@Configuration
@ConfigurationProperties(prefix = "clinic.timezone")
public class ClinicTimezoneConfig {

    /**
     * The timezone ID for the clinic location.
     * Must be a valid IANA timezone identifier (e.g., "Asia/Dubai", "Asia/Amman").
     * Defaults to "Asia/Amman" if not specified.
     */
    private String zoneId = "Asia/Amman";

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        // Validate that the timezone is valid
        try {
            ZoneId.of(zoneId);
            this.zoneId = zoneId;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone ID: " + zoneId, e);
        }
    }

    /**
     * Get the ZoneId object for the configured timezone.
     * Note: This is not a bean property (doesn't follow JavaBean naming convention with "Zone")
     */
    public ZoneId toZoneId() {
        return ZoneId.of(zoneId);
    }
}
