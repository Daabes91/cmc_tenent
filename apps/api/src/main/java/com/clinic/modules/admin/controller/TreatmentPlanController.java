package com.clinic.modules.admin.controller;

import com.clinic.modules.admin.dto.*;
import com.clinic.modules.admin.service.TreatmentPlanService;
import com.clinic.modules.core.treatment.TreatmentPlanStatus;
import com.clinic.security.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing treatment plans.
 * Base path: /admin/treatment-plans
 */
@RestController
@RequestMapping("/admin/treatment-plans")
public class TreatmentPlanController {

    private final TreatmentPlanService treatmentPlanService;

    public TreatmentPlanController(TreatmentPlanService treatmentPlanService) {
        this.treatmentPlanService = treatmentPlanService;
    }

    /**
     * Create a new treatment plan.
     * POST /admin/treatment-plans
     */
    @PostMapping
    @PreAuthorize("@permissionService.canCreate('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> createTreatmentPlan(
            @Valid @RequestBody TreatmentPlanRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        TreatmentPlanResponse response = treatmentPlanService.createTreatmentPlan(request, staffId, staffName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get treatment plan by ID.
     * GET /admin/treatment-plans/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.canView('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> getTreatmentPlan(@PathVariable Long id) {
        TreatmentPlanResponse response = treatmentPlanService.getTreatmentPlan(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all treatment plans.
     * GET /admin/treatment-plans
     */
    @GetMapping
    @PreAuthorize("@permissionService.canView('treatmentPlans')")
    public ResponseEntity<List<TreatmentPlanResponse>> getAllTreatmentPlans(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) TreatmentPlanStatus status
    ) {
        List<TreatmentPlanResponse> response;

        if (patientId != null) {
            response = treatmentPlanService.getTreatmentPlansByPatient(patientId);
        } else if (doctorId != null) {
            response = treatmentPlanService.getTreatmentPlansByDoctor(doctorId);
        } else if (status != null) {
            response = treatmentPlanService.getTreatmentPlansByStatus(status);
        } else {
            response = treatmentPlanService.getAllTreatmentPlans();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update treatment plan pricing and follow-ups.
     * PUT /admin/treatment-plans/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> updateTreatmentPlan(
            @PathVariable Long id,
            @Valid @RequestBody TreatmentPlanUpdateRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        TreatmentPlanResponse response = treatmentPlanService.updateTreatmentPlan(id, request, staffId, staffName);
        return ResponseEntity.ok(response);
    }

    /**
     * Apply discount to treatment plan.
     * POST /admin/treatment-plans/{id}/discount
     */
    @PostMapping("/{id}/discount")
    @PreAuthorize("@permissionService.canEdit('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> applyDiscount(
            @PathVariable Long id,
            @Valid @RequestBody DiscountRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        TreatmentPlanResponse response = treatmentPlanService.applyDiscount(id, request, staffId, staffName);
        return ResponseEntity.ok(response);
    }

    /**
     * Record a follow-up visit with payments and materials.
     * POST /admin/treatment-plans/{id}/visits
     */
    @PostMapping("/{id}/visits")
    @PreAuthorize("@permissionService.canEdit('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> recordFollowUpVisit(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpVisitRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        TreatmentPlanResponse response = treatmentPlanService.recordFollowUpVisit(id, request, staffId, staffName);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a recorded follow-up visit.
     * PUT /admin/treatment-plans/{planId}/visits/{visitId}
     */
    @PutMapping("/{planId}/visits/{visitId}")
    @PreAuthorize("@permissionService.canEdit('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> updateFollowUpVisit(
            @PathVariable Long planId,
            @PathVariable Long visitId,
            @Valid @RequestBody FollowUpVisitRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        TreatmentPlanResponse response = treatmentPlanService.updateFollowUpVisit(planId, visitId, request, staffId, staffName);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete a treatment plan.
     * POST /admin/treatment-plans/{id}/complete
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("@permissionService.canEdit('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> completeTreatmentPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        TreatmentPlanResponse response = treatmentPlanService.completeTreatmentPlan(id, staffId, staffName);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a treatment plan.
     * POST /admin/treatment-plans/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("@permissionService.canDelete('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> cancelTreatmentPlan(
            @PathVariable Long id,
            @RequestBody(required = false) CancelRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        String reason = request != null && request.reason() != null ? request.reason() : "No reason provided";
        TreatmentPlanResponse response = treatmentPlanService.cancelTreatmentPlan(id, reason, staffId, staffName);
        return ResponseEntity.ok(response);
    }

    /**
     * Record a direct payment against a treatment plan.
     * POST /admin/treatment-plans/{id}/payments
     */
    @PostMapping("/{id}/payments")
    @PreAuthorize("@permissionService.canEdit('treatmentPlans')")
    public ResponseEntity<TreatmentPlanResponse> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody TreatmentPlanPaymentRequest request,
            Authentication authentication
    ) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Long staffId = Long.parseLong(principal.subject());
        String staffName = "Admin User"; // TODO: Get staff name from database

        TreatmentPlanResponse response = treatmentPlanService.recordDirectPayment(id, request, staffId, staffName);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all payments for a treatment plan.
     * GET /admin/treatment-plans/{id}/payments
     */
    @GetMapping("/{id}/payments")
    @PreAuthorize("@permissionService.canView('treatmentPlans')")
    public ResponseEntity<List<TreatmentPlanPaymentResponse>> getTreatmentPlanPayments(@PathVariable Long id) {
        List<TreatmentPlanPaymentResponse> payments = treatmentPlanService.getTreatmentPlanPayments(id);
        return ResponseEntity.ok(payments);
    }

    /**
     * Simple DTO for cancel request.
     */
    public record CancelRequest(String reason) {
    }
}
