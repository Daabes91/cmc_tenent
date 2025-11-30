package com.clinic.modules.saas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution.
 * Used for processing webhook events in the background.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configure thread pool for webhook processing.
     * 
     * @return Executor for async tasks
     */
    @Bean(name = "webhookTaskExecutor")
    public Executor webhookTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("webhook-");
        executor.initialize();
        return executor;
    }
}
