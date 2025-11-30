package com.clinic.modules.saas.job;

import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.model.SubscriptionEntity;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.saas.service.BillingAuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to process pending plan changes and cancellations.
 * Runs daily at 2:00 AM to check for effective dates that have passed.
 * 
 * This job handles:
 * - Applying pending plan tier changes (downgrades)
 * - Processing scheduled cancellations
 * - Clearing pending fields after processing
 * - Logging all changes to audit trail
 */
@Component
public class ScheduledPlanChangeJob {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledPlanChangeJob.class);

    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final BillingAuditLogger auditLogger;

    public ScheduledPlanChangeJob(
            SubscriptionRepository subscriptionRepository,
            TenantRepository tenantRepository,
            BillingAuditLogger auditLogger) {
        this.subscriptionRepository = subscriptionRepository;
        this.tenantRepository = tenantRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * Process pending plan changes.
     * Runs daily at 2:00 AM server time.
     * Cron expression: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processPendingPlanChanges() {
        logger.info("Starting scheduled job to process pending plan changes");
        
        try {
            int planChangesProcessed = processPendingPlanTierChanges();
            int cancellationsProcessed = processPendingCancellations();
            
            logger.info("Scheduled job completed successfully. Plan changes: {}, Cancellations: {}", 
                planChangesProcessed, cancellationsProcessed);
                
        } catch (Exception e) {
            logger.error("Error during scheduled plan change processing", e);
            auditLogger.logFailure("SCHEDULED_PLAN_CHANGE_JOB", null, null, 
                "Error processing scheduled plan changes", e);
        }
    }

    /**
     * Process subscriptions with pending plan tier changes.
     * Updates planTier from pendingPlanTier when effective date has passed.
     * 
     * @return number of plan changes processed
     */
    private int processPendingPlanTierChanges() {
        logger.info("Processing pending plan tier changes");
        
        LocalDateTime now = LocalDateTime.now();
        List<SubscriptionEntity> subscriptionsWithPendingChanges = 
            subscriptionRepository.findByPendingPlanTierIsNotNullAndPendingPlanEffectiveDateBefore(now);
        
        int processed = 0;
        for (SubscriptionEntity subscription : subscriptionsWithPendingChanges) {
            try {
                processPlanTierChange(subscription);
                processed++;
            } catch (Exception e) {
                logger.error("Error processing plan tier change for subscription: {}", 
                    subscription.getId(), e);
                auditLogger.logFailure("PROCESS_PENDING_PLAN_CHANGE", 
                    subscription.getTenant().getId(), null,
                    "Error processing pending plan change for subscription: " + subscription.getId(), e);
            }
        }
        
        logger.info("Processed {} pending plan tier changes", processed);
        return processed;
    }

    /**
     * Process a single plan tier change.
     * 
     * @param subscription the subscription with pending plan change
     */
    private void processPlanTierChange(SubscriptionEntity subscription) {
        String oldTier = subscription.getPlanTier() != null ? subscription.getPlanTier().name() : "BASIC";
        String newTier = subscription.getPendingPlanTier().name();
        Long tenantId = subscription.getTenant().getId();
        
        logger.info("Applying pending plan change for tenant: {} from {} to {}", 
            tenantId, oldTier, newTier);
        
        // Apply the plan change
        subscription.setPlanTier(subscription.getPendingPlanTier());
        
        // Clear pending fields
        subscription.setPendingPlanTier(null);
        subscription.setPendingPlanEffectiveDate(null);
        
        subscriptionRepository.save(subscription);
        
        // Log the change
        auditLogger.logPlanChange(tenantId, oldTier, newTier, "Scheduled plan change applied");
        auditLogger.logSubscriptionEvent("PLAN_CHANGE_APPLIED", 
            subscription.getPaypalSubscriptionId(), tenantId,
            "Scheduled plan change from " + oldTier + " to " + newTier + " applied successfully");
        
        logger.info("Successfully applied plan change for tenant: {}", tenantId);
    }

    /**
     * Process subscriptions with pending cancellations.
     * Updates billing_status from active to canceled when cancellation effective date has passed.
     * 
     * @return number of cancellations processed
     */
    private int processPendingCancellations() {
        logger.info("Processing pending cancellations");
        
        LocalDateTime now = LocalDateTime.now();
        List<SubscriptionEntity> subscriptionsWithPendingCancellations = 
            subscriptionRepository.findByCancellationEffectiveDateBeforeAndStatusNot(now, "CANCELLED");
        
        int processed = 0;
        for (SubscriptionEntity subscription : subscriptionsWithPendingCancellations) {
            try {
                processCancellation(subscription);
                processed++;
            } catch (Exception e) {
                logger.error("Error processing cancellation for subscription: {}", 
                    subscription.getId(), e);
                auditLogger.logFailure("PROCESS_PENDING_CANCELLATION", 
                    subscription.getTenant().getId(), null,
                    "Error processing pending cancellation for subscription: " + subscription.getId(), e);
            }
        }
        
        logger.info("Processed {} pending cancellations", processed);
        return processed;
    }

    /**
     * Process a single cancellation.
     * 
     * @param subscription the subscription with pending cancellation
     */
    private void processCancellation(SubscriptionEntity subscription) {
        TenantEntity tenant = subscription.getTenant();
        Long tenantId = tenant.getId();
        String oldStatus = tenant.getBillingStatus().name();
        
        logger.info("Applying pending cancellation for tenant: {}", tenantId);
        
        // Update subscription status
        subscription.setStatus("CANCELLED");
        subscriptionRepository.save(subscription);
        
        // Update tenant billing status
        tenant.setBillingStatus(BillingStatus.CANCELED);
        tenantRepository.save(tenant);
        
        // Log the cancellation
        auditLogger.logBillingStatusChange(tenantId, oldStatus, "CANCELED", 
            "Scheduled cancellation applied");
        auditLogger.logSubscriptionEvent("CANCELLATION_APPLIED", 
            subscription.getPaypalSubscriptionId(), tenantId,
            "Scheduled cancellation applied successfully");
        
        logger.info("Successfully applied cancellation for tenant: {}", tenantId);
    }
}
