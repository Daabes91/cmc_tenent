package com.clinic.modules.saas.service;

import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import com.clinic.modules.saas.config.PlanTierConfig;
import com.clinic.modules.saas.exception.PlanLimitExceededException;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for validating subscription plan limits.
 * Checks if tenants are within their plan's resource limits (patients, staff, doctors).
 */
@Service
public class PlanLimitValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(PlanLimitValidationService.class);
    
    private final SubscriptionRepository subscriptionRepository;
    private final PatientRepository patientRepository;
    private final PlanTierConfig planTierConfig;
    private final TenantService tenantService;
    
    public PlanLimitValidationService(
            SubscriptionRepository subscriptionRepository,
            PatientRepository patientRepository,
            PlanTierConfig planTierConfig,
            TenantService tenantService) {
        this.subscriptionRepository = subscriptionRepository;
        this.patientRepository = patientRepository;
        this.planTierConfig = planTierConfig;
        this.tenantService = tenantService;
    }
    
    /**
     * Validates if a tenant can add a new patient based on their plan limits.
     * 
     * @param tenantId The tenant ID to check
     * @throws PlanLimitExceededException if the limit is exceeded
     */
    @Transactional(readOnly = true)
    public void validatePatientLimit(Long tenantId) {
        PlanTier planTier = getTenantPlanTier(tenantId);
        
        if (planTier == null) {
            log.warn("No subscription found for tenant {}. Allowing operation.", tenantId);
            return; // No subscription = allow (for backwards compatibility)
        }
        
        PlanTierConfig.PlanTierDetails planDetails = planTierConfig.getPlanDetails(planTier);
        if (planDetails == null) {
            log.warn("No plan details found for tier {}. Allowing operation.", planTier);
            return;
        }
        
        int maxPatients = planDetails.getMaxPatients();
        
        // -1 means unlimited
        if (maxPatients == -1) {
            return;
        }
        
        long currentPatientCount = patientRepository.countByTenantId(tenantId);
        
        if (currentPatientCount >= maxPatients) {
            log.warn("Tenant {} exceeded patient limit. Current: {}, Max: {}", 
                    tenantId, currentPatientCount, maxPatients);
            throw new PlanLimitExceededException("patients", (int) currentPatientCount, maxPatients);
        }
    }
    
    /**
     * Validates if a tenant can add a new staff member based on their plan limits.
     * 
     * @param tenantId The tenant ID to check
     * @throws PlanLimitExceededException if the limit is exceeded
     */
    @Transactional(readOnly = true)
    public void validateStaffLimit(Long tenantId) {
        PlanTier planTier = getTenantPlanTier(tenantId);
        
        if (planTier == null) {
            return;
        }
        
        PlanTierConfig.PlanTierDetails planDetails = planTierConfig.getPlanDetails(planTier);
        if (planDetails == null) {
            return;
        }
        
        int maxStaff = planDetails.getMaxStaff();
        
        // -1 means unlimited
        if (maxStaff == -1) {
            return;
        }
        
        // TODO: Implement staff counting when staff management is added
        // long currentStaffCount = staffRepository.countByTenantId(tenantId);
        // if (currentStaffCount >= maxStaff) {
        //     throw new PlanLimitExceededException("staff", (int) currentStaffCount, maxStaff);
        // }
    }
    
    /**
     * Validates if a tenant can add a new doctor based on their plan limits.
     * 
     * @param tenantId The tenant ID to check
     * @throws PlanLimitExceededException if the limit is exceeded
     */
    @Transactional(readOnly = true)
    public void validateDoctorLimit(Long tenantId) {
        PlanTier planTier = getTenantPlanTier(tenantId);
        
        if (planTier == null) {
            return;
        }
        
        PlanTierConfig.PlanTierDetails planDetails = planTierConfig.getPlanDetails(planTier);
        if (planDetails == null) {
            return;
        }
        
        int maxDoctors = planDetails.getMaxDoctors();
        
        // -1 means unlimited
        if (maxDoctors == -1) {
            return;
        }
        
        // TODO: Implement doctor counting when doctor management is added
        // long currentDoctorCount = doctorRepository.countByTenantId(tenantId);
        // if (currentDoctorCount >= maxDoctors) {
        //     throw new PlanLimitExceededException("doctors", (int) currentDoctorCount, maxDoctors);
        // }
    }
    
    /**
     * Gets the current usage statistics for a tenant.
     * 
     * @param tenantId The tenant ID
     * @return Usage statistics including current counts and limits
     */
    @Transactional(readOnly = true)
    public PlanUsageStats getUsageStats(Long tenantId) {
        PlanTier planTier = getTenantPlanTier(tenantId);
        
        if (planTier == null) {
            return new PlanUsageStats(0, -1, 0, -1, 0, -1, null);
        }
        
        PlanTierConfig.PlanTierDetails planDetails = planTierConfig.getPlanDetails(planTier);
        if (planDetails == null) {
            return new PlanUsageStats(0, -1, 0, -1, 0, -1, planTier);
        }
        
        long currentPatients = patientRepository.countByTenantId(tenantId);
        
        return new PlanUsageStats(
                (int) currentPatients,
                planDetails.getMaxPatients(),
                0, // TODO: Add staff count
                planDetails.getMaxStaff(),
                0, // TODO: Add doctor count
                planDetails.getMaxDoctors(),
                planTier
        );
    }
    
    /**
     * Gets the plan tier for a tenant.
     * Cached for 5 minutes to reduce database queries.
     */
    @Cacheable(value = "tenantPlanTier", key = "#tenantId", cacheManager = "billingSaasCacheManager")
    private PlanTier getTenantPlanTier(Long tenantId) {
        TenantEntity tenant = tenantService.requireTenant(tenantId);
        if (tenant == null) {
            return null;
        }
        
        SubscriptionEntity subscription = subscriptionRepository
                .findByTenantId(tenantId)
                .filter(sub -> "ACTIVE".equals(sub.getStatus()) || "TRIALING".equals(sub.getStatus()))
                .orElse(null);
        
        if (subscription == null) {
            return null;
        }
        
        return subscription.getPlanTier();
    }
    
    /**
     * Data class for plan usage statistics.
     */
    public static class PlanUsageStats {
        private final int currentPatients;
        private final int maxPatients;
        private final int currentStaff;
        private final int maxStaff;
        private final int currentDoctors;
        private final int maxDoctors;
        private final PlanTier planTier;
        
        public PlanUsageStats(int currentPatients, int maxPatients, 
                            int currentStaff, int maxStaff,
                            int currentDoctors, int maxDoctors,
                            PlanTier planTier) {
            this.currentPatients = currentPatients;
            this.maxPatients = maxPatients;
            this.currentStaff = currentStaff;
            this.maxStaff = maxStaff;
            this.currentDoctors = currentDoctors;
            this.maxDoctors = maxDoctors;
            this.planTier = planTier;
        }
        
        public int getCurrentPatients() {
            return currentPatients;
        }
        
        public int getMaxPatients() {
            return maxPatients;
        }
        
        public int getCurrentStaff() {
            return currentStaff;
        }
        
        public int getMaxStaff() {
            return maxStaff;
        }
        
        public int getCurrentDoctors() {
            return currentDoctors;
        }
        
        public int getMaxDoctors() {
            return maxDoctors;
        }
        
        public PlanTier getPlanTier() {
            return planTier;
        }
        
        public boolean isPatientLimitReached() {
            return maxPatients != -1 && currentPatients >= maxPatients;
        }
        
        public boolean isStaffLimitReached() {
            return maxStaff != -1 && currentStaff >= maxStaff;
        }
        
        public boolean isDoctorLimitReached() {
            return maxDoctors != -1 && currentDoctors >= maxDoctors;
        }
    }
}
