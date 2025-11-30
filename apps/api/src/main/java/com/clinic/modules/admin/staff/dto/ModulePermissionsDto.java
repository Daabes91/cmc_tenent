package com.clinic.modules.admin.staff.dto;

import com.clinic.modules.admin.staff.model.PermissionAction;

import java.util.Set;

/**
 * DTO for module permissions.
 */
public record ModulePermissionsDto(
        Set<PermissionAction> appointments,
        Set<PermissionAction> calendar,
        Set<PermissionAction> patients,
        Set<PermissionAction> doctors,
        Set<PermissionAction> materials,
        Set<PermissionAction> services,
        Set<PermissionAction> insuranceCompanies,
        Set<PermissionAction> treatmentPlans,
        Set<PermissionAction> reports,
        Set<PermissionAction> billing,
        Set<PermissionAction> translations,
        Set<PermissionAction> settings,
        Set<PermissionAction> clinicSettings,
        Set<PermissionAction> staff,
        Set<PermissionAction> blogs
) {
}
