package com.clinic.modules.saas.service;

import com.clinic.modules.saas.dto.PayPalWebhookEvent;
import com.clinic.modules.saas.monitoring.BillingAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for asynchronous webhook processing with retry logic.
 * Processes webhook events in the background with exponential backoff retry.
 */
@Service
public class AsyncWebhookProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncWebhookProcessor.class);

    private final WebhookService webhookService;
    private final BillingAlertService alertService;

    public AsyncWebhookProcessor(WebhookService webhookService, BillingAlertService alertService) {
        this.webhookService = webhookService;
        this.alertService = alertService;
    }

    /**
     * Process webhook event asynchronously with retry logic.
     * Retries up to 3 times with exponential backoff (1s, 2s, 4s).
     *
     * @param event the webhook event to process
     */
    @Async("webhookTaskExecutor")
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void processWebhookAsync(PayPalWebhookEvent event) {
        logger.info("Processing webhook event asynchronously: {} (ID: {})", 
            event.getEventType(), event.getId());

        try {
            webhookService.processWebhookEvent(event);
            logger.info("Successfully processed webhook event: {} (ID: {})", 
                event.getEventType(), event.getId());
        } catch (Exception e) {
            logger.error("Error processing webhook event: {} (ID: {}). Will retry.", 
                event.getEventType(), event.getId(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Recovery method called after all retry attempts fail.
     * Logs the failure and creates an alert for manual review.
     *
     * @param e the exception that caused the failure
     * @param event the webhook event that failed to process
     */
    @Recover
    public void recoverFromWebhookProcessingFailure(Exception e, PayPalWebhookEvent event) {
        logger.error("ALERT: Failed to process webhook event after 3 retries: {} (ID: {}). Manual review required.", 
            event.getEventType(), event.getId(), e);

        // Trigger alert for failed webhook processing after retries
        alertService.alertWebhookProcessingFailedAfterRetries(
            event.getId(),
            event.getEventType(),
            e.getMessage()
        );

        // In production, this should also:
        // 1. Store failed event in a dead letter queue
        // 2. Create a ticket for manual investigation
        
        logger.error("CRITICAL ALERT: Webhook processing failed permanently. Event Type: {}, Event ID: {}, Error: {}", 
            event.getEventType(), event.getId(), e.getMessage());
    }
}
