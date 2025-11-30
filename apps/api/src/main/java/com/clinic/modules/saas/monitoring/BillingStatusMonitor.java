package com.clinic.modules.saas.monitoring;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for monitoring billing status distribution across tenants.
 * Updates metrics periodically for monitoring dashboards.
 */
@Service
public class BillingStatusMonitor {

    private static final Logger logger = LoggerFactory.getLogger(BillingStatusMonitor.class);

    private final TenantRepository tenantRepository;
    private final BillingMetricsService metricsService;

    public BillingStatusMonitor(TenantRepository tenantRepository, BillingMetricsService metricsService) {
        this.tenantRepository = tenantRepository;
        this.metricsService = metricsService;
    }

    /**
     * Update billing status distribution metrics.
     * Runs every 2 minutes to keep metrics current.
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    public void updateBillingStatusDistribution() {
        try {
            // Count tenants by billing status
            Map<BillingStatus, Long> distribution = tenantRepository.countByBillingStatusGrouped();

            // Update metrics for each status
            for (BillingStatus status : BillingStatus.values()) {
                long count = distribution.getOrDefault(status, 0L);
                metricsService.updateBillingStatusDistribution(status.name(), count);
            }

            logger.debug("Updated billing status distribution metrics: {}", distribution);

        } catch (Exception e) {
            logger.error("Error updating billing status distribution metrics", e);
        }
    }

    /**
     * Log billing status summary.
     * Runs every hour for operational visibility.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void logBillingStatusSummary() {
        try {
            Map<BillingStatus, Long> distribution = tenantRepository.countByBillingStatusGrouped();

            long total = distribution.values().stream().mapToLong(Long::longValue).sum();
            long active = distribution.getOrDefault(BillingStatus.ACTIVE, 0L);
            long pendingPayment = distribution.getOrDefault(BillingStatus.PENDING_PAYMENT, 0L);
            long pastDue = distribution.getOrDefault(BillingStatus.PAST_DUE, 0L);
            long canceled = distribution.getOrDefault(BillingStatus.CANCELED, 0L);

            logger.info("Billing Status Summary - Total: {}, Active: {}, Pending: {}, Past Due: {}, Canceled: {}",
                    total, active, pendingPayment, pastDue, canceled);

            // Calculate percentages
            if (total > 0) {
                double activePercent = (active * 100.0) / total;
                double pendingPercent = (pendingPayment * 100.0) / total;
                double pastDuePercent = (pastDue * 100.0) / total;
                double canceledPercent = (canceled * 100.0) / total;

                logger.info("Billing Status Percentages - Active: {:.2f}%, Pending: {:.2f}%, Past Due: {:.2f}%, Canceled: {:.2f}%",
                        activePercent, pendingPercent, pastDuePercent, canceledPercent);
            }

        } catch (Exception e) {
            logger.error("Error logging billing status summary", e);
        }
    }
}
