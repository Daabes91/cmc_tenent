package com.clinic.modules.admin.auth.dto;

import com.clinic.modules.admin.staff.dto.ModulePermissionsDto;

public record StaffProfileResponse(
        Long id,
        String email,
        String fullName,
        String role,
        ModulePermissionsDto permissions
) {
}
