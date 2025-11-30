package com.clinic.modules.saas.service;

import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.patient.PatientRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating and providing system metrics and analytics
 */
@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private final TenantRepository tenantRepository;
    private final StaffUserRepository staffUserRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public MetricsService(
            TenantRepository tenantRepository,
            StaffUserRepository staffUserRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository) {
        this.tenantRepository = tenantRepository;
        this.staffUserRepository = staffUserRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Get system-wide metrics including tenant and user statistics
     */
    public SystemMetricsResponse getSystemMetrics() {
        log.debug("Calculating system metrics");

        // Count total tenants (excluding soft-deleted)
        long totalTenants = tenantRepository.count();

        // Count active tenants
        long activeTenants = tenantRepository.findAll().stream()
                .filter(t -> t.getStatus() == TenantStatus.ACTIVE && !t.isDeleted())
                .count();

        // Count total users (all staff users)
        long totalUsers = staffUserRepository.count();

        // Count active users
        long activeUsers = staffUserRepository.findAll().stream()
                .filter(staff -> staff.getStatus() == com.clinic.modules.admin.staff.model.StaffStatus.ACTIVE)
                .count();

        // Mock API response time (TODO: implement actual monitoring)
        long apiResponseTime = 50L; // milliseconds

        // Database status (TODO: implement actual health check)
        String databaseStatus = "healthy";

        // Get recent activity (TODO: implement activity logging)
        List<ActivityResponse> recentActivity = new ArrayList<>();

        log.debug("System metrics calculated - tenants: {}/{}, users: {}/{}",
                activeTenants, totalTenants, activeUsers, totalUsers);

        return new SystemMetricsResponse(
                totalTenants,
                activeTenants,
                totalUsers,
                activeUsers,
                apiResponseTime,
                databaseStatus,
                recentActivity
        );
    }

    /**
     * Get analytics data for dashboards and reporting
     */
    public AnalyticsResponse getAnalytics(String timeRange) {
        log.debug("Calculating analytics for timeRange: {}", timeRange);

        // TODO: Implement actual analytics calculation based on time range
        // For now, return placeholder data

        Map<String, Object> tenantGrowth = new HashMap<>();
        tenantGrowth.put("labels", List.of("Week 1", "Week 2", "Week 3", "Week 4"));
        tenantGrowth.put("data", List.of(5, 12, 18, 24));

        Map<String, Object> userActivity = new HashMap<>();
        userActivity.put("labels", List.of("Active", "Inactive"));
        userActivity.put("data", List.of(0, 0));

        Map<String, Object> systemPerformance = new HashMap<>();
        systemPerformance.put("uptime", "99.9%");
        systemPerformance.put("avgResponseTime", 50);

        return new AnalyticsResponse(
                tenantGrowth,
                userActivity,
                systemPerformance
        );
    }

    /**
     * Retrieve high level usage metrics for a specific tenant.
     *
     * @param tenantId Tenant identifier
     * @return metrics snapshot for the tenant
     */
    public TenantMetricsResponse getTenantMetrics(Long tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant with ID " + tenantId + " not found"));

        long staffCount = staffUserRepository.countByTenantId(tenantId);
        long patientCount = patientRepository.countByTenantId(tenantId);
        long appointmentCount = appointmentRepository.countByTenantId(tenantId);

        // For now we treat "userCount" as the number of staff accounts.
        long userCount = staffCount;

        // Storage metrics not yet tracked - surface zero to avoid misleading data.
        double storageUsedMb = 0d;

        return new TenantMetricsResponse(
                tenantId,
                userCount,
                staffCount,
                patientCount,
                appointmentCount,
                storageUsedMb,
                tenant.getUpdatedAt() != null ? tenant.getUpdatedAt() : tenant.getCreatedAt()
        );
    }
}
