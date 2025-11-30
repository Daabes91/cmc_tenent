package com.clinic.modules.saas.service;

import com.clinic.modules.saas.model.BillingAuditLogEntity;
import com.clinic.modules.saas.repository.BillingAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized logging utility for billing operations.
 * Provides structured logging for audit trails and monitoring.
 */
@Component
public class BillingAuditLogger {

    private static final Logger logger = LoggerFactory.getLogger("BILLING_AUDIT");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final BillingAuditLogRepository auditLogRepository;
    private final TransactionTemplate auditTxTemplate;

    public BillingAuditLogger(BillingAuditLogRepository auditLogRepository,
                              PlatformTransactionManager transactionManager) {
        this.auditLogRepository = auditLogRepository;
        this.auditTxTemplate = new TransactionTemplate(transactionManager);
        this.auditTxTemplate.setReadOnly(false);
        this.auditTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void logSuccess(String operation, Long tenantId, Long userId, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[SUCCESS] operation={} | tenant_id={} | user_id={} | timestamp={} | details={}",
                operation, tenantId, userId != null ? userId : "N/A", timestamp, details);
        persistAuditEntry(operation, "INFO", tenantId, userId, details, Map.of("status", "SUCCESS"));
    }

    public void logFailure(String operation, Long tenantId, Long userId, String error, Throwable exception) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        if (exception != null) {
            logger.error("[FAILURE] operation={} | tenant_id={} | user_id={} | timestamp={} | error={} | exception={}",
                    operation, tenantId != null ? tenantId : "N/A", userId != null ? userId : "N/A",
                    timestamp, error, exception.getClass().getSimpleName(), exception);
        } else {
            logger.error("[FAILURE] operation={} | tenant_id={} | user_id={} | timestamp={} | error={}",
                    operation, tenantId != null ? tenantId : "N/A", userId != null ? userId : "N/A",
                    timestamp, error);
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("status", "FAILURE");
        metadata.put("error", error);
        if (exception != null) {
            metadata.put("exception", exception.getClass().getSimpleName());
        }
        persistAuditEntry(operation, "ERROR", tenantId, userId, error, metadata);
    }

    public void logPayPalApiCall(String operation, Long tenantId, String paypalId, Integer statusCode, boolean success) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String status = success ? "SUCCESS" : "FAILURE";
        logger.info("[PAYPAL_API] operation={} | tenant_id={} | paypal_id={} | status_code={} | status={} | timestamp={}",
                operation, tenantId, paypalId, statusCode, status, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("paypalId", paypalId);
        metadata.put("statusCode", statusCode);
        metadata.put("status", status);
        persistAuditEntry(operation, success ? "INFO" : "WARN", tenantId, null,
                String.format("PayPal API call %s", status.toLowerCase()), metadata);
    }

    public void logPayPalApiError(String operation, Long tenantId, String paypalId,
                                   Integer statusCode, String errorMessage, String responseBody) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.error("[PAYPAL_API_ERROR] operation={} | tenant_id={} | paypal_id={} | status_code={} | " +
                     "error={} | response_body={} | timestamp={}",
                operation, tenantId, paypalId != null ? paypalId : "N/A", statusCode,
                errorMessage, responseBody != null ? responseBody : "N/A", timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("paypalId", paypalId);
        metadata.put("statusCode", statusCode);
        metadata.put("responseBody", responseBody);
        persistAuditEntry(operation, "ERROR", tenantId, null, errorMessage, metadata);
    }

    public void logWebhookReceived(String eventType, String eventId, String subscriptionId, Long tenantId) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[WEBHOOK_RECEIVED] event_type={} | event_id={} | subscription_id={} | tenant_id={} | timestamp={}",
                eventType, eventId, subscriptionId, tenantId != null ? tenantId : "N/A", timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventId", eventId);
        metadata.put("subscriptionId", subscriptionId);
        persistAuditEntry(eventType, "INFO", tenantId, null, "Webhook received", metadata);
    }

    public void logWebhookProcessed(String eventType, String eventId, Long tenantId, boolean success, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String status = success ? "SUCCESS" : "FAILURE";
        logger.info("[WEBHOOK_PROCESSED] event_type={} | event_id={} | tenant_id={} | status={} | details={} | timestamp={}",
                eventType, eventId, tenantId, status, details, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventId", eventId);
        metadata.put("status", status);
        persistAuditEntry(eventType + "_PROCESSED", success ? "INFO" : "ERROR", tenantId, null, details, metadata);
    }

    public void logWebhookVerificationFailure(String transmissionId, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.error("[SECURITY_ALERT] type=WEBHOOK_VERIFICATION_FAILED | transmission_id={} | reason={} | timestamp={}",
                transmissionId, reason, timestamp);
        persistAuditEntry("WEBHOOK_VERIFICATION_FAILED", "ERROR", null, null, reason,
                Map.of("transmissionId", transmissionId));
    }

    public void logBillingStatusChange(Long tenantId, String oldStatus, String newStatus, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[BILLING_STATUS_CHANGE] tenant_id={} | old_status={} | new_status={} | reason={} | timestamp={}",
                tenantId, oldStatus, newStatus, reason, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldStatus", oldStatus);
        metadata.put("newStatus", newStatus);
        persistAuditEntry("BILLING_STATUS_CHANGE", "INFO", tenantId, null, reason, metadata);
    }

    public void logSubscriptionEvent(String operation, String subscriptionId, Long tenantId, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[SUBSCRIPTION_EVENT] operation={} | subscription_id={} | tenant_id={} | details={} | timestamp={}",
                operation, subscriptionId, tenantId, details, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("subscriptionId", subscriptionId);
        persistAuditEntry(operation, "INFO", tenantId, null, details, metadata);
    }

    public void logPaymentTransaction(String transactionId, String subscriptionId, Long tenantId,
                                       String amount, String currency, String status) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[PAYMENT_TRANSACTION] transaction_id={} | subscription_id={} | tenant_id={} | " +
                    "amount={} | currency={} | status={} | timestamp={}",
                transactionId, subscriptionId, tenantId, amount, currency, status, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("transactionId", transactionId);
        metadata.put("subscriptionId", subscriptionId);
        metadata.put("amount", amount);
        metadata.put("currency", currency);
        metadata.put("status", status);
        persistAuditEntry("PAYMENT_TRANSACTION", "INFO", tenantId, null,
                "Payment transaction recorded", metadata);
    }

    public void logManualOverride(Long tenantId, Long managerId, String oldStatus, String newStatus, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.warn("[MANUAL_OVERRIDE] tenant_id={} | manager_id={} | old_status={} | new_status={} | " +
                    "reason={} | timestamp={}",
                tenantId, managerId, oldStatus, newStatus, reason, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldStatus", oldStatus);
        metadata.put("newStatus", newStatus);
        metadata.put("reason", reason);
        persistAuditEntry("MANUAL_OVERRIDE", "WARN", tenantId, managerId,
                "Manual billing status override", metadata);
    }

    public void logPlanChange(Long tenantId, String oldTier, String newTier, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[PLAN_CHANGE] tenant_id={} | old_tier={} | new_tier={} | details={} | timestamp={}",
                tenantId, oldTier, newTier, details, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldTier", oldTier);
        metadata.put("newTier", newTier);
        persistAuditEntry("PLAN_CHANGE", "INFO", tenantId, null, details, metadata);
    }

    public void logScheduledPlanChange(Long tenantId, String oldTier, String newTier, String scheduledDate) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[SCHEDULED_PLAN_CHANGE] tenant_id={} | old_tier={} | new_tier={} | effective={} | timestamp={}",
                tenantId, oldTier, newTier, scheduledDate, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldTier", oldTier);
        metadata.put("newTier", newTier);
        metadata.put("effectiveDate", scheduledDate);
        persistAuditEntry("SCHEDULED_PLAN_CHANGE", "INFO", tenantId, null,
                "Scheduled plan change created", metadata);
    }

    public void logPlanOverride(Long tenantId, Long managerId, String oldTier, String newTier, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.warn("[PLAN_OVERRIDE] tenant_id={} | manager_id={} | old_tier={} | new_tier={} | reason={} | timestamp={}",
                tenantId, managerId, oldTier, newTier, reason, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldTier", oldTier);
        metadata.put("newTier", newTier);
        metadata.put("reason", reason);
        persistAuditEntry("PLAN_OVERRIDE", "WARN", tenantId, managerId, reason, metadata);
    }

    public void logPlanUpgrade(Long tenantId, Long managerId, String oldTier, String newTier, LocalDateTime effectiveDate) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[PLAN_UPGRADE] tenant_id={} | manager_id={} | old_tier={} | new_tier={} | effective={} | timestamp={}",
                tenantId, managerId, oldTier, newTier, effectiveDate, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldTier", oldTier);
        metadata.put("newTier", newTier);
        metadata.put("effectiveDate", effectiveDate);
        persistAuditEntry("PLAN_UPGRADE", "INFO", tenantId, managerId,
                "Plan upgrade initiated", metadata);
    }

    public void logPlanDowngrade(Long tenantId, Long managerId, String oldTier, String newTier, LocalDateTime effectiveDate) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[PLAN_DOWNGRADE] tenant_id={} | manager_id={} | old_tier={} | new_tier={} | effective={} | timestamp={}",
                tenantId, managerId, oldTier, newTier, effectiveDate, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldTier", oldTier);
        metadata.put("newTier", newTier);
        metadata.put("effectiveDate", effectiveDate);
        persistAuditEntry("PLAN_DOWNGRADE", "INFO", tenantId, managerId,
                "Plan downgrade scheduled", metadata);
    }

    public void logCancellation(Long tenantId, Long managerId, LocalDateTime requestedAt,
                                LocalDateTime effectiveDate, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.warn("[PLAN_CANCELLATION] tenant_id={} | manager_id={} | requested_at={} | effective={} | reason={} | timestamp={}",
                tenantId, managerId, requestedAt, effectiveDate, reason, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestedAt", requestedAt);
        metadata.put("effectiveDate", effectiveDate);
        metadata.put("reason", reason);
        persistAuditEntry("PLAN_CANCELLATION", "WARN", tenantId, managerId,
                "Subscription cancellation requested", metadata);
    }

    public void logManualPlanOverride(Long tenantId, Long managerId, String oldTier, String newTier, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.warn("[MANUAL_PLAN_OVERRIDE] tenant_id={} | manager_id={} | old_tier={} | new_tier={} | reason={} | timestamp={}",
                tenantId, managerId, oldTier, newTier, reason, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldTier", oldTier);
        metadata.put("newTier", newTier);
        metadata.put("reason", reason);
        persistAuditEntry("MANUAL_PLAN_OVERRIDE", "WARN", tenantId, managerId,
                "Plan override executed manually", metadata);
    }

    public void logPaymentMethodUpdate(Long tenantId, String oldMethod, String newMethod, String status) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.info("[PAYMENT_METHOD_UPDATE] tenant_id={} | old_method={} | new_method={} | status={} | timestamp={}",
                tenantId, oldMethod, newMethod, status, timestamp);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldMethod", oldMethod);
        metadata.put("newMethod", newMethod);
        metadata.put("status", status);
        persistAuditEntry("PAYMENT_METHOD_UPDATE", "INFO", tenantId, null,
                "Payment method update completed", metadata);
    }

    private void persistAuditEntry(String action,
                                   String severity,
                                   Long tenantId,
                                   Long managerId,
                                   String description,
                                   Map<String, Object> metadata) {
        try {
            BillingAuditLogEntity entity = new BillingAuditLogEntity();
            entity.setAction(action);
            entity.setSeverity(severity);
            entity.setTenantId(tenantId);
            entity.setManagerId(managerId);
            entity.setDescription(description);
            entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            entity.setMetadata(metadata != null ? metadata : Map.of());
            auditTxTemplate.executeWithoutResult(status -> auditLogRepository.save(entity));
        } catch (Exception e) {
            logger.warn("Failed to persist billing audit log entry for action {}: {}", action, e.getMessage());
        }
    }
}
