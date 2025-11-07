package com.clinic.modules.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffPasswordChangeRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 255, message = "New password must be between 8 and 255 characters")
        String newPassword
) {
}
