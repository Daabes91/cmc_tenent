package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.ClinicSettingsResponse;
import com.clinic.modules.admin.dto.ClinicSettingsUpdateRequest;
import com.clinic.modules.admin.service.ClinicSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/settings")
public class ClinicSettingsController {

    private final ClinicSettingsService settingsService;

    public ClinicSettingsController(ClinicSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.canView('settings') and hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ClinicSettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("@permissionService.canEdit('settings') and hasRole('ADMIN')")
    public ResponseEntity<ClinicSettingsResponse> updateSettings(
            @Valid @RequestBody ClinicSettingsUpdateRequest request) {
        return ResponseEntity.ok(settingsService.updateSettings(request));
    }
}
