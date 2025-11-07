package com.clinic.modules.admin.staff.service;

import com.clinic.modules.admin.staff.model.ModuleName;
import com.clinic.modules.admin.staff.model.PermissionAction;
import com.clinic.modules.admin.staff.model.StaffPermissions;
import com.clinic.modules.admin.staff.repository.StaffPermissionsRepository;
import com.clinic.security.JwtPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for checking staff permissions at runtime.
 * Used by @PreAuthorize expressions and programmatic permission checks.
 */
@Service
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private final StaffPermissionsRepository permissionsRepository;

    public PermissionService(StaffPermissionsRepository permissionsRepository) {
        this.permissionsRepository = permissionsRepository;
    }

    /**
     * Check if the currently authenticated staff has a specific permission.
     *
     * @param module The module to check (e.g., "appointments", "patients")
     * @param action The action to check (e.g., "VIEW", "CREATE", "EDIT", "DELETE")
     * @return true if the staff has the permission, false otherwise
     */
    public boolean hasPermission(String module, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof JwtPrincipal)) {
            log.debug("No authenticated staff principal found");
            return false;
        }

        JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();

        // ADMIN role has all permissions
        if (principal.roles().contains("ROLE_ADMIN")) {
            return true;
        }

        try {
            Long staffId = Long.parseLong(principal.subject());
            // Convert camelCase to SNAKE_CASE (e.g., "treatmentPlans" -> "TREATMENT_PLANS")
            String snakeCase = module.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
            ModuleName moduleName = ModuleName.valueOf(snakeCase);
            PermissionAction permissionAction = PermissionAction.valueOf(action.toUpperCase());

            return hasPermission(staffId, moduleName, permissionAction);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid module or action: module={}, action={}", module, action);
            return false;
        } catch (Exception e) {
            log.error("Error checking permission for module={}, action={}", module, action, e);
            return false;
        }
    }

    /**
     * Check if a specific staff member has a permission.
     */
    public boolean hasPermission(Long staffId, ModuleName module, PermissionAction action) {
        return permissionsRepository.findByStaffUserId(staffId)
            .map(permissions -> {
                Set<PermissionAction> modulePermissions = permissions.getPermissionsForModule(module);
                return modulePermissions != null && modulePermissions.contains(action);
            })
            .orElse(false);
    }

    /**
     * Check if the currently authenticated staff has VIEW permission for a module.
     */
    public boolean canView(String module) {
        return hasPermission(module, "VIEW");
    }

    /**
     * Check if the currently authenticated staff has CREATE permission for a module.
     */
    public boolean canCreate(String module) {
        return hasPermission(module, "CREATE");
    }

    /**
     * Check if the currently authenticated staff has EDIT permission for a module.
     */
    public boolean canEdit(String module) {
        return hasPermission(module, "EDIT");
    }

    /**
     * Check if the currently authenticated staff has DELETE permission for a module.
     */
    public boolean canDelete(String module) {
        return hasPermission(module, "DELETE");
    }

    /**
     * Get the currently authenticated staff ID.
     */
    public Long getCurrentStaffId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal) {
            JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
            try {
                return Long.parseLong(principal.subject());
            } catch (NumberFormatException e) {
                log.error("Failed to parse staff ID from principal subject: {}", principal.subject());
            }
        }

        return null;
    }

    /**
     * Check if the current user is an ADMIN.
     */
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal) {
            JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
            return principal.roles().contains("ROLE_ADMIN");
        }

        return false;
    }
}
