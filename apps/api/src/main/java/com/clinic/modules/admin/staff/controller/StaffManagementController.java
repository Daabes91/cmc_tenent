package com.clinic.modules.admin.staff.controller;

import com.clinic.modules.admin.staff.dto.*;
import com.clinic.modules.admin.staff.service.StaffManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for staff management operations.
 * Only accessible by users with ADMIN role.
 */
@RestController
@RequestMapping("/admin/staff")
public class StaffManagementController {

    private final StaffManagementService staffManagementService;

    public StaffManagementController(StaffManagementService staffManagementService) {
        this.staffManagementService = staffManagementService;
    }

    /**
     * Create a new staff member and send invitation email.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        StaffResponse response = staffManagementService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all staff members.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<StaffResponse>> getAllStaff() {
        List<StaffResponse> staff = staffManagementService.getAllStaff();
        return ResponseEntity.ok(staff);
    }

    /**
     * Get a staff member by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable Long id) {
        StaffResponse staff = staffManagementService.getStaffById(id);
        return ResponseEntity.ok(staff);
    }

    /**
     * Update a staff member.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request
    ) {
        StaffResponse response = staffManagementService.updateStaff(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a staff member.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffManagementService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resend invitation email to a staff member.
     */
    @PostMapping("/{id}/resend-invitation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resendInvitation(@PathVariable Long id) {
        staffManagementService.resendInvitation(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get permissions for a staff member.
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModulePermissionsDto> getStaffPermissions(@PathVariable Long id) {
        ModulePermissionsDto permissions = staffManagementService.getStaffPermissions(id);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Update permissions for a staff member.
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStaffPermissions(
            @PathVariable Long id,
            @Valid @RequestBody ModulePermissionsDto permissions
    ) {
        staffManagementService.updateStaffPermissions(id, permissions);
        return ResponseEntity.ok().build();
    }
}
