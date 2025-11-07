package com.clinic.modules.admin.staff.dto;

import com.clinic.modules.admin.staff.model.PermissionAction;

import java.util.Set;

/**
 * DTO for module permissions.
 */
public record ModulePermissionsDto(
        Set<PermissionAction> appointments,
        Set<PermissionAction> patients,
        Set<PermissionAction> doctors,
        Set<PermissionAction> services,
        Set<PermissionAction> treatmentPlans,
        Set<PermissionAction> reports,
        Set<PermissionAction> settings,
        Set<PermissionAction> staff,
        Set<PermissionAction> blogs
) {
}
