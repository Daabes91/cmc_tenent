package com.clinic.modules.admin.staff.dto;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import jakarta.validation.constraints.Email;

/**
 * Request DTO for updating a staff member.
 * All fields are optional - only provided fields will be updated.
 */
public record UpdateStaffRequest(
        @Email(message = "Invalid email format")
        String email,

        String fullName,

        StaffRole role,

        StaffStatus status,

        ModulePermissionsDto permissions,

        /**
         * Optional doctor ID for staff members with DOCTOR role.
         * Set to null to unlink from doctor profile.
         */
        Long doctorId
) {
}
