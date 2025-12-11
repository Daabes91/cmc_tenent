package com.clinic.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utility for consistent timestamp formatting across all API responses.
 * Ensures standardized ISO 8601 timestamp representation.
 */
public class TimestampFormatter {

    /**
     * Standard ISO 8601 format pattern for API responses.
     */
    public static final String ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    /**
     * Standard DateTimeFormatter for ISO 8601 format.
     */
    public static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter.ofPattern(ISO_8601_PATTERN);

    /**
     * Standardized timestamp representation for API responses.
     */
    public static class ApiTimestamp {
        
        @JsonProperty("timestamp")
        @JsonFormat(pattern = ISO_8601_PATTERN)
        private final Instant timestamp;
        
        @JsonProperty("iso_string")
        private final String isoString;

        public ApiTimestamp(Instant timestamp) {
            this.timestamp = timestamp != null ? timestamp : Instant.now();
            this.isoString = formatToIso8601(this.timestamp);
        }

        public ApiTimestamp(LocalDateTime localDateTime) {
            this.timestamp = localDateTime != null ? localDateTime.toInstant(ZoneOffset.UTC) : Instant.now();
            this.isoString = formatToIso8601(this.timestamp);
        }

        public Instant getTimestamp() { return timestamp; }
        public String getIsoString() { return isoString; }

        @Override
        public String toString() {
            return isoString;
        }
    }

    /**
     * Create a standardized timestamp from Instant.
     */
    public static ApiTimestamp of(Instant instant) {
        return new ApiTimestamp(instant);
    }

    /**
     * Create a standardized timestamp from LocalDateTime.
     */
    public static ApiTimestamp of(LocalDateTime localDateTime) {
        return new ApiTimestamp(localDateTime);
    }

    /**
     * Create a standardized timestamp for current time.
     */
    public static ApiTimestamp now() {
        return new ApiTimestamp(Instant.now());
    }

    /**
     * Format Instant to ISO 8601 string.
     */
    public static String formatToIso8601(Instant instant) {
        if (instant == null) {
            instant = Instant.now();
        }
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * Format LocalDateTime to ISO 8601 string.
     */
    public static String formatToIso8601(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return formatToIso8601(Instant.now());
        }
        return localDateTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * Parse ISO 8601 string to Instant.
     */
    public static Instant parseFromIso8601(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Instant.parse(isoString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO 8601 timestamp: " + isoString, e);
        }
    }

    /**
     * Validate ISO 8601 timestamp string.
     */
    public static boolean isValidIso8601(String isoString) {
        try {
            parseFromIso8601(isoString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current timestamp as ISO 8601 string.
     */
    public static String getCurrentIso8601() {
        return formatToIso8601(Instant.now());
    }
}