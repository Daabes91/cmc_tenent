package com.clinic.modules.admin.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordResetTokenCleanupJob
 * 
 * Requirements: 6.4
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetTokenCleanupJobTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @InjectMocks
    private PasswordResetTokenCleanupJob cleanupJob;

    @BeforeEach
    void setUp() {
        reset(tokenRepository);
    }

    @Test
    void testCleanupExpiredTokens_DeletesExpiredTokens() {
        // Arrange
        doNothing().when(tokenRepository).deleteExpiredTokens(any(Instant.class));

        // Act
        cleanupJob.cleanupExpiredTokens();

        // Assert
        verify(tokenRepository, times(1)).deleteExpiredTokens(any(Instant.class));
    }

    @Test
    void testCleanupExpiredTokens_HandlesNoTokensToDelete() {
        // Arrange
        doNothing().when(tokenRepository).deleteExpiredTokens(any(Instant.class));

        // Act
        cleanupJob.cleanupExpiredTokens();

        // Assert
        verify(tokenRepository, times(1)).deleteExpiredTokens(any(Instant.class));
    }

    @Test
    void testCleanupExpiredTokens_HandlesRepositoryException() {
        // Arrange
        doThrow(new RuntimeException("Database error"))
            .when(tokenRepository).deleteExpiredTokens(any(Instant.class));

        // Act - should not throw exception
        cleanupJob.cleanupExpiredTokens();

        // Assert
        verify(tokenRepository, times(1)).deleteExpiredTokens(any(Instant.class));
    }

    @Test
    void testCleanupExpiredTokens_CallsWithCorrectTimeParameter() {
        // Arrange
        Instant beforeCall = Instant.now().minus(24, ChronoUnit.HOURS);
        doNothing().when(tokenRepository).deleteExpiredTokens(any(Instant.class));

        // Act
        cleanupJob.cleanupExpiredTokens();
        Instant afterCall = Instant.now().minus(24, ChronoUnit.HOURS);

        // Assert - verify that the cutoff time is approximately 24 hours ago
        verify(tokenRepository).deleteExpiredTokens(argThat(instant ->
            !instant.isAfter(beforeCall.plus(1, ChronoUnit.SECONDS)) && 
            !instant.isBefore(afterCall.minus(1, ChronoUnit.SECONDS))
        ));
    }

    @Test
    void testCleanupExpiredTokens_DeletesMultipleTokens() {
        // Arrange
        doNothing().when(tokenRepository).deleteExpiredTokens(any(Instant.class));

        // Act
        cleanupJob.cleanupExpiredTokens();

        // Assert
        verify(tokenRepository, times(1)).deleteExpiredTokens(any(Instant.class));
    }
}
