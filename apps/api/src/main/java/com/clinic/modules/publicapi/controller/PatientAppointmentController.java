package com.clinic.modules.publicapi.controller;

import com.clinic.modules.publicapi.dto.PatientAppointmentResponse;
import com.clinic.modules.publicapi.service.PatientAppointmentService;
import com.clinic.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/appointments")
public class PatientAppointmentController {

    private final PatientAppointmentService patientAppointmentService;

    public PatientAppointmentController(PatientAppointmentService patientAppointmentService) {
        this.patientAppointmentService = patientAppointmentService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<PatientAppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Long patientId = Long.parseLong(principal.subject());
        var appointments = patientAppointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }
}
