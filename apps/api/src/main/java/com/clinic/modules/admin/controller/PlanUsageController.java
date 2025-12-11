package com.clinic.modules.admin.controller;

import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.saas.service.PlanLimitValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for exposing plan usage statistics to clinic admins.
 */
@RestController
@RequestMapping("/api/admin/plan-usage")
public class PlanUsageController {
    
    private final PlanLimitValidationService planLimitValidationService;
    private final TenantContextHolder tenantContextHolder;
    
    public PlanUsageController(
            PlanLimitValidationService planLimitValidationService,
            TenantContextHolder tenantContextHolder) {
        this.planLimitValidationService = planLimitValidationService;
        this.tenantContextHolder = tenantContextHolder;
    }
    
    /**
     * Get current plan usage statistics for the authenticated tenant.
     * 
     * @return Usage statistics including current counts and limits
     */
    @GetMapping
    public ResponseEntity<PlanUsageResponse> getUsageStats() {
        Long tenantId = tenantContextHolder.requireTenantId();
        PlanLimitValidationService.PlanUsageStats stats = 
                planLimitValidationService.getUsageStats(tenantId);
        
        return ResponseEntity.ok(new PlanUsageResponse(
                stats.getCurrentPatients(),
                stats.getMaxPatients(),
                stats.getCurrentStaff(),
                stats.getMaxStaff(),
                stats.getCurrentDoctors(),
                stats.getMaxDoctors(),
                stats.getPlanTier() != null ? stats.getPlanTier().name() : null,
                stats.isPatientLimitReached(),
                stats.isStaffLimitReached(),
                stats.isDoctorLimitReached()
        ));
    }
    
    /**
     * Response DTO for plan usage statistics.
     */
    public record PlanUsageResponse(
            int currentPatients,
            int maxPatients,
            int currentStaff,
            int maxStaff,
            int currentDoctors,
            int maxDoctors,
            String planTier,
            boolean patientLimitReached,
            boolean staffLimitReached,
            boolean doctorLimitReached
    ) {}
}
