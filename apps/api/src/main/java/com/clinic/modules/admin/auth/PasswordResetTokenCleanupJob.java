package com.clinic.modules.admin.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled job to clean up expired password reset tokens.
 * Runs daily to remove tokens older than 24 hours from the database.
 * 
 * This job handles:
 * - Deleting expired password reset tokens (older than 24 hours)
 * - Logging cleanup statistics
 * 
 * Requirements: 6.4
 */
@Component
public class PasswordResetTokenCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenCleanupJob.class);
    
    // Delete tokens older than 24 hours
    private static final int TOKEN_RETENTION_HOURS = 24;

    private final PasswordResetTokenRepository tokenRepository;

    public PasswordResetTokenCleanupJob(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Clean up expired password reset tokens.
     * Runs daily at 2:00 AM.
     * Cron expression: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("Starting scheduled password reset token cleanup job");
        
        try {
            Instant cutoffTime = Instant.now().minus(TOKEN_RETENTION_HOURS, ChronoUnit.HOURS);
            
            // Delete tokens older than 24 hours
            tokenRepository.deleteExpiredTokens(cutoffTime);
            
            logger.info("Password reset token cleanup completed successfully. Deleted tokens older than {} hours", 
                TOKEN_RETENTION_HOURS);
                
        } catch (Exception e) {
            logger.error("Error during password reset token cleanup", e);
        }
    }
}
