package com.clinic.modules.saas.service;

import com.clinic.modules.saas.exception.PayPalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker implementation for PayPal API calls.
 * Prevents cascading failures by temporarily blocking requests when PayPal is unavailable.
 * 
 * States:
 * - CLOSED: Normal operation, requests pass through
 * - OPEN: Too many failures, requests are blocked
 * - HALF_OPEN: Testing if service has recovered
 */
@Component
public class PayPalCircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(PayPalCircuitBreaker.class);

    private static final int FAILURE_THRESHOLD = 5; // Open circuit after 5 failures
    private static final int SUCCESS_THRESHOLD = 2; // Close circuit after 2 successes in half-open state
    private static final long TIMEOUT_DURATION_SECONDS = 60; // Wait 60 seconds before trying again

    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>();

    /**
     * Check if the circuit breaker allows the request to proceed.
     * 
     * @throws PayPalApiException if circuit is open
     */
    public void checkState() {
        State currentState = state.get();

        if (currentState == State.OPEN) {
            LocalDateTime lastFailure = lastFailureTime.get();
            if (lastFailure != null && 
                LocalDateTime.now().isAfter(lastFailure.plusSeconds(TIMEOUT_DURATION_SECONDS))) {
                // Timeout has passed, transition to half-open
                logger.info("Circuit breaker transitioning from OPEN to HALF_OPEN");
                state.set(State.HALF_OPEN);
                successCount.set(0);
            } else {
                // Circuit is still open, reject request
                logger.warn("Circuit breaker is OPEN, rejecting PayPal API request");
                throw new PayPalApiException("circuit breaker", 
                    "PayPal service is temporarily unavailable. Please try again in a few moments.");
            }
        }
    }

    /**
     * Record a successful API call.
     */
    public void recordSuccess() {
        State currentState = state.get();

        if (currentState == State.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            if (successes >= SUCCESS_THRESHOLD) {
                // Enough successes, close the circuit
                logger.info("Circuit breaker transitioning from HALF_OPEN to CLOSED after {} successes", successes);
                state.set(State.CLOSED);
                failureCount.set(0);
                successCount.set(0);
            }
        } else if (currentState == State.CLOSED) {
            // Reset failure count on success
            failureCount.set(0);
        }
    }

    /**
     * Record a failed API call.
     */
    public void recordFailure() {
        State currentState = state.get();
        lastFailureTime.set(LocalDateTime.now());

        if (currentState == State.HALF_OPEN) {
            // Failure in half-open state, reopen circuit
            logger.warn("Circuit breaker transitioning from HALF_OPEN to OPEN after failure");
            state.set(State.OPEN);
            successCount.set(0);
        } else if (currentState == State.CLOSED) {
            int failures = failureCount.incrementAndGet();
            if (failures >= FAILURE_THRESHOLD) {
                // Too many failures, open circuit
                logger.error("Circuit breaker transitioning from CLOSED to OPEN after {} failures", failures);
                state.set(State.OPEN);
            }
        }
    }

    /**
     * Get current circuit breaker state.
     */
    public String getState() {
        return state.get().name();
    }

    /**
     * Get current failure count.
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * Reset the circuit breaker (for testing or manual intervention).
     */
    public void reset() {
        logger.info("Circuit breaker manually reset");
        state.set(State.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        lastFailureTime.set(null);
    }
}
