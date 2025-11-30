package com.clinic.modules.core.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled job to clean up expired and consumed OAuth states.
 * Runs every hour to remove stale state tokens from the database.
 * 
 * This job handles:
 * - Deleting expired OAuth states (past their expiration time)
 * - Deleting consumed OAuth states older than 1 hour
 * - Logging cleanup statistics
 */
@Component
public class OAuthStateCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(OAuthStateCleanupJob.class);
    
    // Keep consumed states for 1 hour for debugging purposes
    private static final int CONSUMED_STATE_RETENTION_HOURS = 1;

    private final OAuthStateRepository oauthStateRepository;

    public OAuthStateCleanupJob(OAuthStateRepository oauthStateRepository) {
        this.oauthStateRepository = oauthStateRepository;
    }

    /**
     * Clean up expired and consumed OAuth states.
     * Runs every hour at the top of the hour.
     * Cron expression: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupOAuthStates() {
        logger.info("Starting scheduled OAuth state cleanup job");
        
        try {
            Instant now = Instant.now();
            
            // Delete expired states
            int expiredDeleted = oauthStateRepository.deleteExpiredStates(now);
            logger.info("Deleted {} expired OAuth states", expiredDeleted);
            
            // Delete consumed states older than retention period
            Instant cutoffTime = now.minus(CONSUMED_STATE_RETENTION_HOURS, ChronoUnit.HOURS);
            int consumedDeleted = oauthStateRepository.deleteConsumedStates(cutoffTime);
            logger.info("Deleted {} consumed OAuth states older than {} hour(s)", 
                consumedDeleted, CONSUMED_STATE_RETENTION_HOURS);
            
            logger.info("OAuth state cleanup completed successfully. Total deleted: {}", 
                expiredDeleted + consumedDeleted);
                
        } catch (Exception e) {
            logger.error("Error during OAuth state cleanup", e);
        }
    }
}
