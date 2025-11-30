package com.clinic.modules.core.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OAuthStateCleanupJob
 */
@ExtendWith(MockitoExtension.class)
class OAuthStateCleanupJobTest {

    @Mock
    private OAuthStateRepository oauthStateRepository;

    @InjectMocks
    private OAuthStateCleanupJob cleanupJob;

    @BeforeEach
    void setUp() {
        reset(oauthStateRepository);
    }

    @Test
    void testCleanupOAuthStates_DeletesExpiredStates() {
        // Arrange
        when(oauthStateRepository.deleteExpiredStates(any(Instant.class)))
            .thenReturn(5);
        when(oauthStateRepository.deleteConsumedStates(any(Instant.class)))
            .thenReturn(0);

        // Act
        cleanupJob.cleanupOAuthStates();

        // Assert
        verify(oauthStateRepository, times(1)).deleteExpiredStates(any(Instant.class));
        verify(oauthStateRepository, times(1)).deleteConsumedStates(any(Instant.class));
    }

    @Test
    void testCleanupOAuthStates_DeletesConsumedStates() {
        // Arrange
        when(oauthStateRepository.deleteExpiredStates(any(Instant.class)))
            .thenReturn(0);
        when(oauthStateRepository.deleteConsumedStates(any(Instant.class)))
            .thenReturn(3);

        // Act
        cleanupJob.cleanupOAuthStates();

        // Assert
        verify(oauthStateRepository, times(1)).deleteExpiredStates(any(Instant.class));
        verify(oauthStateRepository, times(1)).deleteConsumedStates(any(Instant.class));
    }

    @Test
    void testCleanupOAuthStates_DeletesBothExpiredAndConsumed() {
        // Arrange
        when(oauthStateRepository.deleteExpiredStates(any(Instant.class)))
            .thenReturn(7);
        when(oauthStateRepository.deleteConsumedStates(any(Instant.class)))
            .thenReturn(4);

        // Act
        cleanupJob.cleanupOAuthStates();

        // Assert
        verify(oauthStateRepository, times(1)).deleteExpiredStates(any(Instant.class));
        verify(oauthStateRepository, times(1)).deleteConsumedStates(any(Instant.class));
    }

    @Test
    void testCleanupOAuthStates_HandlesNoStatesToDelete() {
        // Arrange
        when(oauthStateRepository.deleteExpiredStates(any(Instant.class)))
            .thenReturn(0);
        when(oauthStateRepository.deleteConsumedStates(any(Instant.class)))
            .thenReturn(0);

        // Act
        cleanupJob.cleanupOAuthStates();

        // Assert
        verify(oauthStateRepository, times(1)).deleteExpiredStates(any(Instant.class));
        verify(oauthStateRepository, times(1)).deleteConsumedStates(any(Instant.class));
    }

    @Test
    void testCleanupOAuthStates_HandlesRepositoryException() {
        // Arrange
        when(oauthStateRepository.deleteExpiredStates(any(Instant.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act - should not throw exception
        cleanupJob.cleanupOAuthStates();

        // Assert
        verify(oauthStateRepository, times(1)).deleteExpiredStates(any(Instant.class));
        // Should not call deleteConsumedStates if deleteExpiredStates throws
        verify(oauthStateRepository, never()).deleteConsumedStates(any(Instant.class));
    }

    @Test
    void testCleanupOAuthStates_ContinuesAfterExpiredDeletionError() {
        // Arrange
        when(oauthStateRepository.deleteExpiredStates(any(Instant.class)))
            .thenReturn(5);
        when(oauthStateRepository.deleteConsumedStates(any(Instant.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act - should not throw exception
        cleanupJob.cleanupOAuthStates();

        // Assert
        verify(oauthStateRepository, times(1)).deleteExpiredStates(any(Instant.class));
        verify(oauthStateRepository, times(1)).deleteConsumedStates(any(Instant.class));
    }

    @Test
    void testCleanupOAuthStates_CallsWithCorrectTimeParameters() {
        // Arrange
        Instant beforeCall = Instant.now();
        when(oauthStateRepository.deleteExpiredStates(any(Instant.class)))
            .thenReturn(0);
        when(oauthStateRepository.deleteConsumedStates(any(Instant.class)))
            .thenReturn(0);

        // Act
        cleanupJob.cleanupOAuthStates();
        Instant afterCall = Instant.now();

        // Assert
        verify(oauthStateRepository).deleteExpiredStates(argThat(instant ->
            !instant.isBefore(beforeCall) && !instant.isAfter(afterCall)
        ));
        
        verify(oauthStateRepository).deleteConsumedStates(argThat(instant ->
            instant.isBefore(beforeCall) // Should be 1 hour before now
        ));
    }
}
