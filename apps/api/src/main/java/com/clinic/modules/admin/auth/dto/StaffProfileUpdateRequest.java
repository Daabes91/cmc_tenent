package com.clinic.modules.admin.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffProfileUpdateRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 160, message = "Full name must be 160 characters or less")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(max = 160, message = "Email must be 160 characters or less")
        String email
) {
}
