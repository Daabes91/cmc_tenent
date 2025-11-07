package com.clinic.modules.core.patient;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class PatientContactUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private PatientContactUtils() {
    }

    public static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "Email is required.");
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return normalized;
    }

    public static String normalizePhone(String phone) {
        Objects.requireNonNull(phone, "Phone number is required.");
        String cleaned = phone.trim().replace(" ", "").replace("-", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Phone number must include a country code in international format, e.g. +15551234567.");
        }
        return cleaned;
    }
}
