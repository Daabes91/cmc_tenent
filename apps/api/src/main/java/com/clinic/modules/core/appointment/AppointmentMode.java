package com.clinic.modules.core.appointment;

import java.util.Locale;

public enum AppointmentMode {
    CLINIC_VISIT,
    VIRTUAL_CONSULTATION;

    public static AppointmentMode from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Booking mode is required.");
        }
        String normalized = value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CLINIC", "CLINIC_VISIT", "IN_PERSON", "CLINIC_VISITATION" -> CLINIC_VISIT;
            case "VIRTUAL", "VIRTUAL_CONSULTATION", "ONLINE", "VIDEO" -> VIRTUAL_CONSULTATION;
            default -> throw new IllegalArgumentException("Unsupported booking mode: " + value);
        };
    }

    public String displayName() {
        return switch (this) {
            case CLINIC_VISIT -> "Clinic Visit";
            case VIRTUAL_CONSULTATION -> "Virtual Consultation";
        };
    }
}
