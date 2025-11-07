package com.clinic.modules.publicapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Guest booking request for users without authentication.
 * Phone number is required to create or identify guest patient.
 */
public record GuestBookingRequest(
        @NotBlank(message = "Service slug is required") String serviceSlug,
        Long doctorId, // Optional - will auto-assign if not provided
        @NotBlank(message = "Slot is required") String slot,
        @NotBlank(message = "Consultation type is required (clinic/virtual)") String consultationType,
        @NotBlank(message = "Phone number is required") String phoneNumber,
        @Email(message = "Guest email must be a valid email address")
        @Size(max = 160, message = "Guest email must be 160 characters or fewer")
        String guestEmail,
        String guestName, // Optional but recommended
        String notes
) {
}
