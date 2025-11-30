package com.clinic.modules.saas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for billing monitoring and alerting.
 * Enables scheduled tasks for metrics collection and alert checking.
 */
@Configuration
@EnableScheduling
public class MonitoringConfig {
    // Scheduling is enabled for:
    // - BillingAlertService: Periodic alert checks
    // - BillingStatusMonitor: Billing status distribution updates
}
