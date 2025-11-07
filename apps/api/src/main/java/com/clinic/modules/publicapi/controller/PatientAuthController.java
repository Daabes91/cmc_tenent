package com.clinic.modules.publicapi.controller;

import com.clinic.modules.publicapi.dto.PatientAuthResponse;
import com.clinic.modules.publicapi.dto.PatientLoginRequest;
import com.clinic.modules.publicapi.dto.PatientSignupRequest;
import com.clinic.modules.publicapi.service.PatientAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/auth")
public class PatientAuthController {

    private final PatientAuthService patientAuthService;

    public PatientAuthController(PatientAuthService patientAuthService) {
        this.patientAuthService = patientAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<PatientAuthResponse> signup(@Valid @RequestBody PatientSignupRequest request) {
        return ResponseEntity.ok(patientAuthService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<PatientAuthResponse> login(@Valid @RequestBody PatientLoginRequest request) {
        return ResponseEntity.ok(patientAuthService.login(request));
    }
}
