package com.clinic.modules.publicapi.controller;

import com.clinic.modules.admin.dto.TreatmentPlanResponse;
import com.clinic.modules.admin.service.TreatmentPlanService;
import com.clinic.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public API controller for patients to view their treatment plans.
 * Base path: /public/treatment-plans
 */
@RestController
@RequestMapping("/public/treatment-plans")
public class PatientTreatmentPlanController {

    private final TreatmentPlanService treatmentPlanService;

    public PatientTreatmentPlanController(TreatmentPlanService treatmentPlanService) {
        this.treatmentPlanService = treatmentPlanService;
    }

    /**
     * Get all treatment plans for the authenticated patient.
     * GET /public/treatment-plans/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<TreatmentPlanResponse>> getMyTreatmentPlans(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long patientId = Long.parseLong(principal.subject());

        List<TreatmentPlanResponse> response = treatmentPlanService.getTreatmentPlansByPatient(patientId);
        return ResponseEntity.ok(response);
    }
}
