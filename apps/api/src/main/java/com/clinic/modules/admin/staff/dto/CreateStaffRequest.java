package com.clinic.modules.admin.staff.dto;

import com.clinic.modules.admin.staff.model.StaffRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new staff member.
 */
public record CreateStaffRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotNull(message = "Role is required")
        StaffRole role,

        @NotNull(message = "Permissions are required")
        ModulePermissionsDto permissions,

        /**
         * Optional doctor ID for staff members with DOCTOR role.
         * Links the staff account to an existing doctor profile.
         */
        Long doctorId
) {
}
