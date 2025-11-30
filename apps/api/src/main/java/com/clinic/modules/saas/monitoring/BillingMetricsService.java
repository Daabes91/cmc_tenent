package com.clinic.modules.saas.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for tracking billing operation metrics.
 * Provides counters and timers for monitoring billing system health.
 */
@Service
public class BillingMetricsService {

    private final MeterRegistry meterRegistry;

    // Webhook metrics
    private final Counter webhookReceivedCounter;
    private final Counter webhookProcessedSuccessCounter;
    private final Counter webhookProcessedFailureCounter;
    private final Counter webhookVerificationFailureCounter;
    private final Timer webhookProcessingTimer;

    // PayPal API metrics
    private final Counter paypalApiCallCounter;
    private final Counter paypalApiSuccessCounter;
    private final Counter paypalApiErrorCounter;
    private final Timer paypalApiResponseTimer;

    // Subscription metrics
    private final Counter subscriptionCreatedCounter;
    private final Counter subscriptionCreationFailureCounter;
    private final Counter subscriptionActivatedCounter;
    private final Counter subscriptionCancelledCounter;
    private final Counter subscriptionSuspendedCounter;

    // Billing status metrics
    private final Counter billingStatusChangeCounter;

    public BillingMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize webhook metrics
        this.webhookReceivedCounter = Counter.builder("billing.webhook.received")
                .description("Total number of webhooks received from PayPal")
                .tag("component", "billing")
                .register(meterRegistry);

        this.webhookProcessedSuccessCounter = Counter.builder("billing.webhook.processed.success")
                .description("Number of webhooks processed successfully")
                .tag("component", "billing")
                .register(meterRegistry);

        this.webhookProcessedFailureCounter = Counter.builder("billing.webhook.processed.failure")
                .description("Number of webhooks that failed processing")
                .tag("component", "billing")
                .register(meterRegistry);

        this.webhookVerificationFailureCounter = Counter.builder("billing.webhook.verification.failure")
                .description("Number of webhook signature verification failures (SECURITY ALERT)")
                .tag("component", "billing")
                .tag("severity", "critical")
                .register(meterRegistry);

        this.webhookProcessingTimer = Timer.builder("billing.webhook.processing.time")
                .description("Time taken to process webhooks")
                .tag("component", "billing")
                .register(meterRegistry);

        // Initialize PayPal API metrics
        this.paypalApiCallCounter = Counter.builder("billing.paypal.api.calls")
                .description("Total number of PayPal API calls")
                .tag("component", "billing")
                .register(meterRegistry);

        this.paypalApiSuccessCounter = Counter.builder("billing.paypal.api.success")
                .description("Number of successful PayPal API calls")
                .tag("component", "billing")
                .register(meterRegistry);

        this.paypalApiErrorCounter = Counter.builder("billing.paypal.api.error")
                .description("Number of failed PayPal API calls")
                .tag("component", "billing")
                .tag("severity", "high")
                .register(meterRegistry);

        this.paypalApiResponseTimer = Timer.builder("billing.paypal.api.response.time")
                .description("PayPal API response time")
                .tag("component", "billing")
                .register(meterRegistry);

        // Initialize subscription metrics
        this.subscriptionCreatedCounter = Counter.builder("billing.subscription.created")
                .description("Number of subscriptions created")
                .tag("component", "billing")
                .register(meterRegistry);

        this.subscriptionCreationFailureCounter = Counter.builder("billing.subscription.creation.failure")
                .description("Number of subscription creation failures")
                .tag("component", "billing")
                .tag("severity", "high")
                .register(meterRegistry);

        this.subscriptionActivatedCounter = Counter.builder("billing.subscription.activated")
                .description("Number of subscriptions activated")
                .tag("component", "billing")
                .register(meterRegistry);

        this.subscriptionCancelledCounter = Counter.builder("billing.subscription.cancelled")
                .description("Number of subscriptions cancelled")
                .tag("component", "billing")
                .register(meterRegistry);

        this.subscriptionSuspendedCounter = Counter.builder("billing.subscription.suspended")
                .description("Number of subscriptions suspended")
                .tag("component", "billing")
                .tag("severity", "medium")
                .register(meterRegistry);

        // Initialize billing status metrics
        this.billingStatusChangeCounter = Counter.builder("billing.status.change")
                .description("Number of billing status changes")
                .tag("component", "billing")
                .register(meterRegistry);
    }

    // Webhook metrics methods

    public void recordWebhookReceived(String eventType) {
        webhookReceivedCounter.increment();
        Counter.builder("billing.webhook.received.by.type")
                .description("Webhooks received by event type")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .increment();
    }

    public void recordWebhookProcessedSuccess(String eventType) {
        webhookProcessedSuccessCounter.increment();
        Counter.builder("billing.webhook.processed.success.by.type")
                .description("Webhooks processed successfully by event type")
                .tag("event_type", eventType)
                .register(meterRegistry)
                .increment();
    }

    public void recordWebhookProcessedFailure(String eventType, String errorType) {
        webhookProcessedFailureCounter.increment();
        Counter.builder("billing.webhook.processed.failure.by.type")
                .description("Webhooks that failed processing by event type")
                .tag("event_type", eventType)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }

    public void recordWebhookVerificationFailure() {
        webhookVerificationFailureCounter.increment();
    }

    public void recordWebhookProcessingTime(long durationMs) {
        webhookProcessingTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public Timer.Sample startWebhookProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopWebhookProcessingTimer(Timer.Sample sample) {
        sample.stop(webhookProcessingTimer);
    }

    // PayPal API metrics methods

    public void recordPayPalApiCall(String operation) {
        paypalApiCallCounter.increment();
        Counter.builder("billing.paypal.api.calls.by.operation")
                .description("PayPal API calls by operation")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    public void recordPayPalApiSuccess(String operation, int statusCode) {
        paypalApiSuccessCounter.increment();
        Counter.builder("billing.paypal.api.success.by.operation")
                .description("Successful PayPal API calls by operation")
                .tag("operation", operation)
                .tag("status_code", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }

    public void recordPayPalApiError(String operation, int statusCode) {
        paypalApiErrorCounter.increment();
        Counter.builder("billing.paypal.api.error.by.operation")
                .description("Failed PayPal API calls by operation")
                .tag("operation", operation)
                .tag("status_code", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }

    public void recordPayPalApiResponseTime(String operation, long durationMs) {
        Timer.builder("billing.paypal.api.response.time.by.operation")
                .description("PayPal API response time by operation")
                .tag("operation", operation)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public Timer.Sample startPayPalApiTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPayPalApiTimer(Timer.Sample sample, String operation) {
        sample.stop(Timer.builder("billing.paypal.api.response.time.by.operation")
                .tag("operation", operation)
                .register(meterRegistry));
    }

    // Subscription metrics methods

    public void recordSubscriptionCreated() {
        subscriptionCreatedCounter.increment();
    }

    public void recordSubscriptionCreationFailure(String reason) {
        subscriptionCreationFailureCounter.increment();
        Counter.builder("billing.subscription.creation.failure.by.reason")
                .description("Subscription creation failures by reason")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordSubscriptionActivated() {
        subscriptionActivatedCounter.increment();
    }

    public void recordSubscriptionCancelled() {
        subscriptionCancelledCounter.increment();
    }

    public void recordSubscriptionSuspended() {
        subscriptionSuspendedCounter.increment();
    }

    // Billing status metrics methods

    public void recordBillingStatusChange(String fromStatus, String toStatus) {
        billingStatusChangeCounter.increment();
        Counter.builder("billing.status.change.by.transition")
                .description("Billing status changes by transition")
                .tag("from_status", fromStatus)
                .tag("to_status", toStatus)
                .register(meterRegistry)
                .increment();
    }

    // Gauge methods for current state

    public void updateBillingStatusDistribution(String status, long count) {
        meterRegistry.gauge("billing.status.distribution",
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("status", status)),
                count);
    }

    // Success rate calculations (for alerting)

    public double getWebhookSuccessRate() {
        double success = webhookProcessedSuccessCounter.count();
        double failure = webhookProcessedFailureCounter.count();
        double total = success + failure;
        return total > 0 ? (success / total) * 100 : 100.0;
    }

    public double getPayPalApiSuccessRate() {
        double success = paypalApiSuccessCounter.count();
        double error = paypalApiErrorCounter.count();
        double total = success + error;
        return total > 0 ? (success / total) * 100 : 100.0;
    }

    public double getSubscriptionCreationSuccessRate() {
        double success = subscriptionCreatedCounter.count();
        double failure = subscriptionCreationFailureCounter.count();
        double total = success + failure;
        return total > 0 ? (success / total) * 100 : 100.0;
    }
}
