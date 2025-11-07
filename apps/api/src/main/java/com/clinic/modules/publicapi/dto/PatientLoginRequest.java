package com.clinic.modules.publicapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatientLoginRequest(
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(min = 8, max = 64) String password
) {
}
