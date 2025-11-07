package com.clinic.modules.admin.staff.dto;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;

import java.time.Instant;

/**
 * Response DTO for staff member information.
 */
public record StaffResponse(
        Long id,
        String email,
        String fullName,
        StaffRole role,
        StaffStatus status,
        ModulePermissionsDto permissions,
        Boolean hasPendingInvitation,
        Instant createdAt,
        DoctorInfo doctor
) {
    /**
     * Basic doctor information for staff members with DOCTOR role.
     */
    public record DoctorInfo(
            Long id,
            String name,
            String specialty
    ) {
    }
}
