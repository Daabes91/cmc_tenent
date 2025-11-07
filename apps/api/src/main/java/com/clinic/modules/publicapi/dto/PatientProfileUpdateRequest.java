package com.clinic.modules.publicapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientProfileUpdateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone number must include country code (e.g. +15551234567)") @Size(max = 32) String phone,
        @Size(max = 500) String profileImageUrl,
        @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth
) {
}
