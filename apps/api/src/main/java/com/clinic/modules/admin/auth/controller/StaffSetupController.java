package com.clinic.modules.admin.auth.controller;

import com.clinic.modules.admin.staff.dto.SetPasswordRequest;
import com.clinic.modules.admin.staff.service.StaffManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public controller for staff setup operations (password setup via invitation).
 * These endpoints do not require authentication.
 */
@RestController
@RequestMapping("/admin/setup")
public class StaffSetupController {

    private final StaffManagementService staffManagementService;

    public StaffSetupController(StaffManagementService staffManagementService) {
        this.staffManagementService = staffManagementService;
    }

    /**
     * Set password using invitation token.
     * This is a public endpoint for new staff to activate their account.
     */
    @PostMapping("/set-password")
    public ResponseEntity<Void> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        staffManagementService.setPasswordWithToken(request);
        return ResponseEntity.ok().build();
    }
}
