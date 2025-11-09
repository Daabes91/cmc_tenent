package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.AnalyticsResponse;
import com.clinic.modules.saas.dto.SystemMetricsResponse;
import com.clinic.modules.saas.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for system metrics and analytics.
 * All endpoints require SAAS_MANAGER role authentication.
 */
@RestController
@RequestMapping("/saas")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Get system-wide metrics for dashboard
     *
     * @return SystemMetricsResponse with tenant and user statistics (200 OK)
     */
    @GetMapping("/metrics/system")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<SystemMetricsResponse> getSystemMetrics() {
        log.debug("GET /saas/metrics/system - fetching system metrics");
        SystemMetricsResponse metrics = metricsService.getSystemMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get analytics data for reporting and visualization
     *
     * @param timeRange Time range for analytics (7d, 30d, 90d, custom)
     * @param startDate Optional start date for custom range
     * @param endDate Optional end date for custom range
     * @return AnalyticsResponse with chart and report data (200 OK)
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @RequestParam(defaultValue = "30d") String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.debug("GET /saas/analytics - timeRange: {}, startDate: {}, endDate: {}",
                timeRange, startDate, endDate);
        AnalyticsResponse analytics = metricsService.getAnalytics(timeRange);
        return ResponseEntity.ok(analytics);
    }
}
