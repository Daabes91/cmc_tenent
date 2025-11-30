package com.clinic.modules.saas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration to enable Spring Retry for webhook processing.
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
