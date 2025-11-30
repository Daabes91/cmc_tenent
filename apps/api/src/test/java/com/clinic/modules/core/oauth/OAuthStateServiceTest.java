package com.clinic.modules.core.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OAuthStateService.
 * These tests verify specific functionality and edge cases.
 */
class OAuthStateServiceTest {

    private OAuthStateService service;
    private OAuthStateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOAuthStateRepository();
        service = new OAuthStateService(repository);
    }

    @Test
    void generateState_shouldCreateValidState() {
        // Given
        String tenantSlug = "test-clinic";
        String nonce = "test-nonce-123";

        // When
        String stateToken = service.generateState(tenantSlug, nonce);

        // Then
        assertNotNull(stateToken);
        assertFalse(stateToken.isEmpty());
        assertTrue(stateToken.length() >= 32);
    }

    @Test
    void generateState_withRedirectUri_shouldStoreRedirectUri() {
        // Given
        String tenantSlug = "test-clinic";
        String nonce = "test-nonce-123";
        String redirectUri = "https://example.com/callback";

        // When
        String stateToken = service.generateState(tenantSlug, nonce, redirectUri);
        OAuthStateService.OAuthStateData stateData = service.validateAndConsumeState(stateToken);

        // Then
        assertEquals(redirectUri, stateData.redirectUri());
    }

    @Test
    void generateState_shouldThrowException_whenTenantSlugIsNull() {
        // Given
        String nonce = "test-nonce";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.generateState(null, nonce);
        });
    }

    @Test
    void generateState_shouldThrowException_whenTenantSlugIsEmpty() {
        // Given
        String nonce = "test-nonce";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.generateState("", nonce);
        });
    }

    @Test
    void generateState_shouldThrowException_whenNonceIsNull() {
        // Given
        String tenantSlug = "test-clinic";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.generateState(tenantSlug, null);
        });
    }

    @Test
    void validateAndConsumeState_shouldReturnCorrectData() {
        // Given
        String tenantSlug = "test-clinic";
        String nonce = "test-nonce-123";
        String stateToken = service.generateState(tenantSlug, nonce);

        // When
        OAuthStateService.OAuthStateData stateData = service.validateAndConsumeState(stateToken);

        // Then
        assertEquals(tenantSlug, stateData.tenantSlug());
        assertEquals(nonce, stateData.nonce());
    }

    @Test
    void validateAndConsumeState_shouldThrowException_whenTokenIsNull() {
        // When & Then
        assertThrows(InvalidOAuthStateException.class, () -> {
            service.validateAndConsumeState(null);
        });
    }

    @Test
    void validateAndConsumeState_shouldThrowException_whenTokenIsEmpty() {
        // When & Then
        assertThrows(InvalidOAuthStateException.class, () -> {
            service.validateAndConsumeState("");
        });
    }

    @Test
    void validateAndConsumeState_shouldThrowException_whenTokenDoesNotExist() {
        // Given
        String invalidToken = "invalid-token-that-does-not-exist";

        // When & Then
        InvalidOAuthStateException exception = assertThrows(InvalidOAuthStateException.class, () -> {
            service.validateAndConsumeState(invalidToken);
        });
        assertTrue(exception.getMessage().contains("Invalid state token"));
    }

    @Test
    void validateAndConsumeState_shouldThrowException_whenTokenAlreadyConsumed() {
        // Given
        String tenantSlug = "test-clinic";
        String nonce = "test-nonce-123";
        String stateToken = service.generateState(tenantSlug, nonce);

        // Consume once
        service.validateAndConsumeState(stateToken);

        // When & Then - try to consume again
        InvalidOAuthStateException exception = assertThrows(InvalidOAuthStateException.class, () -> {
            service.validateAndConsumeState(stateToken);
        });
        assertTrue(exception.getMessage().contains("already been used"));
    }

    @Test
    void validateAndConsumeState_shouldThrowException_whenTokenExpired() {
        // Given - create an expired state directly in repository
        String stateToken = "expired-token-123";
        String tenantSlug = "test-clinic";
        String nonce = "test-nonce";
        OAuthStateEntity expiredState = new OAuthStateEntity(
            stateToken,
            tenantSlug,
            nonce,
            null,
            Instant.now().minusSeconds(60) // Expired 1 minute ago
        );
        repository.save(expiredState);

        // When & Then
        InvalidOAuthStateException exception = assertThrows(InvalidOAuthStateException.class, () -> {
            service.validateAndConsumeState(stateToken);
        });
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void cleanupExpiredStates_shouldRemoveExpiredStates() {
        // Given - create some expired states
        for (int i = 0; i < 5; i++) {
            OAuthStateEntity expiredState = new OAuthStateEntity(
                "expired-token-" + i,
                "test-clinic",
                "nonce-" + i,
                null,
                Instant.now().minusSeconds(60)
            );
            repository.save(expiredState);
        }

        // Create some valid states
        for (int i = 0; i < 3; i++) {
            service.generateState("test-clinic-" + i, "nonce-" + i);
        }

        // When
        int deletedCount = service.cleanupExpiredStates();

        // Then
        assertEquals(5, deletedCount);
        assertEquals(3, repository.count()); // Only valid states remain
    }

    @Test
    void cleanupConsumedStates_shouldRemoveOldConsumedStates() {
        // Given - create and consume some states
        for (int i = 0; i < 5; i++) {
            String token = service.generateState("test-clinic-" + i, "nonce-" + i);
            service.validateAndConsumeState(token);
        }

        // Manually set created_at to old time for consumed states
        repository.findAll().forEach(state -> {
            if (state.isConsumed()) {
                try {
                    java.lang.reflect.Field createdAtField = OAuthStateEntity.class.getDeclaredField("createdAt");
                    createdAtField.setAccessible(true);
                    createdAtField.set(state, Instant.now().minusSeconds(7200)); // 2 hours ago
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // When - cleanup consumed states older than 1 hour
        int deletedCount = service.cleanupConsumedStates(1);

        // Then
        assertEquals(5, deletedCount);
        assertEquals(0, repository.count());
    }
}
