package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientUpsertRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Email @Size(max = 160) String email,
        @Size(max = 32) String phone,
        @Size(max = 500) String profileImageUrl,
        @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth
) {
}
