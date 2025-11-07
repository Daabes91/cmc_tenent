package com.clinic.modules.publicapi.controller;

import com.clinic.modules.publicapi.dto.PatientProfileResponse;
import com.clinic.modules.publicapi.dto.PatientProfileUpdateRequest;
import com.clinic.modules.publicapi.service.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/patient/me")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping
    public ResponseEntity<PatientProfileResponse> getMyProfile(Authentication authentication) {
        Long patientId = resolvePatientId(authentication);
        if (patientId == null) {
            return ResponseEntity.status(401).build();
        }

        var profile = patientProfileService.getPatientProfile(patientId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<PatientProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody PatientProfileUpdateRequest request
    ) {
        Long patientId = resolvePatientId(authentication);
        if (patientId == null) {
            return ResponseEntity.status(401).build();
        }

        var updated = patientProfileService.updatePatientProfile(patientId, request);
        return ResponseEntity.ok(updated);
    }

    private Long resolvePatientId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.clinic.security.JwtPrincipal jwtPrincipal) {
                return Long.parseLong(jwtPrincipal.subject());
            }
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
