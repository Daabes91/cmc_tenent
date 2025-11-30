package com.clinic.modules.core.oauth;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for tracking Google OAuth operation metrics.
 * Provides counters and timers for monitoring OAuth system health.
 */
@Service
public class OAuthMetricsService {

    private final MeterRegistry meterRegistry;

    // OAuth flow metrics
    private final Counter oauthFlowInitiatedCounter;
    private final Counter oauthFlowSuccessCounter;
    private final Counter oauthFlowFailureCounter;
    private final Counter oauthFlowCancelledCounter;
    private final Timer oauthFlowDurationTimer;

    // Account creation metrics
    private final Counter newPatientCreatedCounter;
    private final Counter accountLinkingSuccessCounter;
    private final Counter accountLinkingFailureCounter;

    // Security metrics
    private final Counter invalidStateCounter;
    private final Counter invalidTokenCounter;
    private final Counter stateTamperingCounter;

    // Google API metrics
    private final Counter googleApiCallCounter;
    private final Counter googleApiSuccessCounter;
    private final Counter googleApiErrorCounter;
    private final Timer googleApiResponseTimer;

    // Error metrics
    private final Counter networkErrorCounter;
    private final Counter tenantErrorCounter;
    private final Counter accountErrorCounter;

    public OAuthMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize OAuth flow metrics
        this.oauthFlowInitiatedCounter = Counter.builder("oauth.flow.initiated")
                .description("Total number of OAuth flows initiated")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        this.oauthFlowSuccessCounter = Counter.builder("oauth.flow.success")
                .description("Number of successful OAuth authentications")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        this.oauthFlowFailureCounter = Counter.builder("oauth.flow.failure")
                .description("Number of failed OAuth authentications")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "high")
                .register(meterRegistry);

        this.oauthFlowCancelledCounter = Counter.builder("oauth.flow.cancelled")
                .description("Number of OAuth flows cancelled by user")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        this.oauthFlowDurationTimer = Timer.builder("oauth.flow.duration")
                .description("Time taken to complete OAuth flow")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        // Initialize account creation metrics
        this.newPatientCreatedCounter = Counter.builder("oauth.patient.created")
                .description("Number of new patients created via OAuth")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        this.accountLinkingSuccessCounter = Counter.builder("oauth.account.linking.success")
                .description("Number of successful account linking operations")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        this.accountLinkingFailureCounter = Counter.builder("oauth.account.linking.failure")
                .description("Number of failed account linking operations")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "medium")
                .register(meterRegistry);

        // Initialize security metrics
        this.invalidStateCounter = Counter.builder("oauth.security.invalid_state")
                .description("Number of invalid state parameter attempts (SECURITY ALERT)")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "critical")
                .register(meterRegistry);

        this.invalidTokenCounter = Counter.builder("oauth.security.invalid_token")
                .description("Number of invalid token attempts (SECURITY ALERT)")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "critical")
                .register(meterRegistry);

        this.stateTamperingCounter = Counter.builder("oauth.security.state_tampering")
                .description("Number of state tampering attempts (SECURITY ALERT)")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "critical")
                .register(meterRegistry);

        // Initialize Google API metrics
        this.googleApiCallCounter = Counter.builder("oauth.google.api.calls")
                .description("Total number of Google API calls")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        this.googleApiSuccessCounter = Counter.builder("oauth.google.api.success")
                .description("Number of successful Google API calls")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        this.googleApiErrorCounter = Counter.builder("oauth.google.api.error")
                .description("Number of failed Google API calls")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "high")
                .register(meterRegistry);

        this.googleApiResponseTimer = Timer.builder("oauth.google.api.response.time")
                .description("Google API response time")
                .tag("component", "oauth")
                .tag("provider", "google")
                .register(meterRegistry);

        // Initialize error metrics
        this.networkErrorCounter = Counter.builder("oauth.error.network")
                .description("Number of network errors during OAuth")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "medium")
                .register(meterRegistry);

        this.tenantErrorCounter = Counter.builder("oauth.error.tenant")
                .description("Number of tenant-related errors during OAuth")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "high")
                .register(meterRegistry);

        this.accountErrorCounter = Counter.builder("oauth.error.account")
                .description("Number of account-related errors during OAuth")
                .tag("component", "oauth")
                .tag("provider", "google")
                .tag("severity", "medium")
                .register(meterRegistry);
    }

    // OAuth flow metrics methods

    public void recordOAuthFlowInitiated(String tenantSlug) {
        oauthFlowInitiatedCounter.increment();
        Counter.builder("oauth.flow.initiated.by.tenant")
                .description("OAuth flows initiated by tenant")
                .tag("tenant_slug", tenantSlug)
                .register(meterRegistry)
                .increment();
    }

    public void recordOAuthFlowSuccess(String tenantSlug, boolean isNewPatient, boolean accountLinked) {
        oauthFlowSuccessCounter.increment();
        Counter.builder("oauth.flow.success.by.tenant")
                .description("Successful OAuth flows by tenant")
                .tag("tenant_slug", tenantSlug)
                .tag("is_new_patient", String.valueOf(isNewPatient))
                .tag("account_linked", String.valueOf(accountLinked))
                .register(meterRegistry)
                .increment();
    }

    public void recordOAuthFlowFailure(String tenantSlug, String errorType) {
        oauthFlowFailureCounter.increment();
        Counter.builder("oauth.flow.failure.by.type")
                .description("Failed OAuth flows by error type")
                .tag("tenant_slug", tenantSlug)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }

    public void recordOAuthFlowCancelled(String tenantSlug) {
        oauthFlowCancelledCounter.increment();
        Counter.builder("oauth.flow.cancelled.by.tenant")
                .description("Cancelled OAuth flows by tenant")
                .tag("tenant_slug", tenantSlug)
                .register(meterRegistry)
                .increment();
    }

    public void recordOAuthFlowDuration(long durationMs) {
        oauthFlowDurationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public Timer.Sample startOAuthFlowTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopOAuthFlowTimer(Timer.Sample sample) {
        sample.stop(oauthFlowDurationTimer);
    }

    // Account creation metrics methods

    public void recordNewPatientCreated(String tenantSlug) {
        newPatientCreatedCounter.increment();
        Counter.builder("oauth.patient.created.by.tenant")
                .description("New patients created via OAuth by tenant")
                .tag("tenant_slug", tenantSlug)
                .register(meterRegistry)
                .increment();
    }

    public void recordAccountLinkingSuccess(String tenantSlug) {
        accountLinkingSuccessCounter.increment();
        Counter.builder("oauth.account.linking.success.by.tenant")
                .description("Successful account linking by tenant")
                .tag("tenant_slug", tenantSlug)
                .register(meterRegistry)
                .increment();
    }

    public void recordAccountLinkingFailure(String tenantSlug, String reason) {
        accountLinkingFailureCounter.increment();
        Counter.builder("oauth.account.linking.failure.by.reason")
                .description("Failed account linking by reason")
                .tag("tenant_slug", tenantSlug)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    // Security metrics methods

    public void recordInvalidState(String reason) {
        invalidStateCounter.increment();
        Counter.builder("oauth.security.invalid_state.by.reason")
                .description("Invalid state attempts by reason")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordInvalidToken(String reason) {
        invalidTokenCounter.increment();
        Counter.builder("oauth.security.invalid_token.by.reason")
                .description("Invalid token attempts by reason")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordStateTampering() {
        stateTamperingCounter.increment();
    }

    // Google API metrics methods

    public void recordGoogleApiCall(String operation) {
        googleApiCallCounter.increment();
        Counter.builder("oauth.google.api.calls.by.operation")
                .description("Google API calls by operation")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    public void recordGoogleApiSuccess(String operation) {
        googleApiSuccessCounter.increment();
        Counter.builder("oauth.google.api.success.by.operation")
                .description("Successful Google API calls by operation")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    public void recordGoogleApiError(String operation, String errorType) {
        googleApiErrorCounter.increment();
        Counter.builder("oauth.google.api.error.by.operation")
                .description("Failed Google API calls by operation")
                .tag("operation", operation)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }

    public void recordGoogleApiResponseTime(String operation, long durationMs) {
        Timer.builder("oauth.google.api.response.time.by.operation")
                .description("Google API response time by operation")
                .tag("operation", operation)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public Timer.Sample startGoogleApiTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopGoogleApiTimer(Timer.Sample sample, String operation) {
        sample.stop(Timer.builder("oauth.google.api.response.time.by.operation")
                .tag("operation", operation)
                .register(meterRegistry));
    }

    // Error metrics methods

    public void recordNetworkError(String operation) {
        networkErrorCounter.increment();
        Counter.builder("oauth.error.network.by.operation")
                .description("Network errors by operation")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    public void recordTenantError(String tenantSlug, String reason) {
        tenantErrorCounter.increment();
        Counter.builder("oauth.error.tenant.by.reason")
                .description("Tenant errors by reason")
                .tag("tenant_slug", tenantSlug != null ? tenantSlug : "unknown")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordAccountError(String reason) {
        accountErrorCounter.increment();
        Counter.builder("oauth.error.account.by.reason")
                .description("Account errors by reason")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    // Success rate calculations (for alerting)

    public double getOAuthFlowSuccessRate() {
        double success = oauthFlowSuccessCounter.count();
        double failure = oauthFlowFailureCounter.count();
        double total = success + failure;
        return total > 0 ? (success / total) * 100 : 100.0;
    }

    public double getGoogleApiSuccessRate() {
        double success = googleApiSuccessCounter.count();
        double error = googleApiErrorCounter.count();
        double total = success + error;
        return total > 0 ? (success / total) * 100 : 100.0;
    }

    public double getAccountLinkingSuccessRate() {
        double success = accountLinkingSuccessCounter.count();
        double failure = accountLinkingFailureCounter.count();
        double total = success + failure;
        return total > 0 ? (success / total) * 100 : 100.0;
    }

    // Gauge methods for current state

    public void updateOAuthFlowDistribution(String status, long count) {
        meterRegistry.gauge("oauth.flow.distribution",
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("status", status)),
                count);
    }
}
