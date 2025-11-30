package com.clinic.modules.core.oauth;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OAuthStateEntity
 */
class OAuthStateEntityTest {

    @Test
    void testConstructor_CreatesEntityWithAllFields() {
        // Arrange
        String stateToken = "test-state-token-123";
        String tenantSlug = "test-clinic";
        String nonce = "test-nonce-456";
        String redirectUri = "https://example.com/callback";
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        // Act
        OAuthStateEntity entity = new OAuthStateEntity(
            stateToken, tenantSlug, nonce, redirectUri, expiresAt
        );

        // Assert
        assertNotNull(entity);
        assertEquals(stateToken, entity.getStateToken());
        assertEquals(tenantSlug, entity.getTenantSlug());
        assertEquals(nonce, entity.getNonce());
        assertEquals(redirectUri, entity.getRedirectUri());
        assertEquals(expiresAt, entity.getExpiresAt());
        assertFalse(entity.isConsumed());
        assertNotNull(entity.getCreatedAt()); // Set in constructor
    }

    @Test
    void testMarkConsumed_SetsConsumedToTrue() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();
        assertFalse(entity.isConsumed());

        // Act
        entity.markConsumed();

        // Assert
        assertTrue(entity.isConsumed());
    }

    @Test
    void testIsExpired_ReturnsTrueForExpiredState() {
        // Arrange - create state that expired 1 minute ago
        Instant pastExpiration = Instant.now().minus(1, ChronoUnit.MINUTES);
        OAuthStateEntity entity = new OAuthStateEntity(
            "token", "tenant", "nonce", "uri", pastExpiration
        );

        // Act & Assert
        assertTrue(entity.isExpired());
    }

    @Test
    void testIsExpired_ReturnsFalseForValidState() {
        // Arrange - create state that expires in 5 minutes
        Instant futureExpiration = Instant.now().plus(5, ChronoUnit.MINUTES);
        OAuthStateEntity entity = new OAuthStateEntity(
            "token", "tenant", "nonce", "uri", futureExpiration
        );

        // Act & Assert
        assertFalse(entity.isExpired());
    }

    @Test
    void testIsValid_ReturnsTrueForUnconsumedUnexpiredState() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();

        // Act & Assert
        assertTrue(entity.isValid());
    }

    @Test
    void testIsValid_ReturnsFalseForConsumedState() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();
        entity.markConsumed();

        // Act & Assert
        assertFalse(entity.isValid());
    }

    @Test
    void testIsValid_ReturnsFalseForExpiredState() {
        // Arrange - create expired state
        Instant pastExpiration = Instant.now().minus(1, ChronoUnit.MINUTES);
        OAuthStateEntity entity = new OAuthStateEntity(
            "token", "tenant", "nonce", "uri", pastExpiration
        );

        // Act & Assert
        assertFalse(entity.isValid());
    }

    @Test
    void testIsValid_ReturnsFalseForConsumedAndExpiredState() {
        // Arrange - create expired and consumed state
        Instant pastExpiration = Instant.now().minus(1, ChronoUnit.MINUTES);
        OAuthStateEntity entity = new OAuthStateEntity(
            "token", "tenant", "nonce", "uri", pastExpiration
        );
        entity.markConsumed();

        // Act & Assert
        assertFalse(entity.isValid());
    }

    @Test
    void testOnCreate_SetsCreatedAtTimestamp() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();
        Instant originalCreatedAt = entity.getCreatedAt();
        assertNotNull(originalCreatedAt); // Already set in constructor

        // Act - simulate @PrePersist (should not overwrite existing createdAt)
        entity.onCreate();

        // Assert - createdAt should remain unchanged
        assertNotNull(entity.getCreatedAt());
        assertEquals(originalCreatedAt, entity.getCreatedAt());
    }

    @Test
    void testEquals_SameId_ReturnsTrue() {
        // Arrange
        OAuthStateEntity entity1 = createTestEntity();
        OAuthStateEntity entity2 = createTestEntity();
        
        // Use reflection to set same ID (normally set by JPA)
        setId(entity1, 1L);
        setId(entity2, 1L);

        // Act & Assert
        assertEquals(entity1, entity2);
    }

    @Test
    void testEquals_DifferentId_ReturnsFalse() {
        // Arrange
        OAuthStateEntity entity1 = createTestEntity();
        OAuthStateEntity entity2 = createTestEntity();
        
        setId(entity1, 1L);
        setId(entity2, 2L);

        // Act & Assert
        assertNotEquals(entity1, entity2);
    }

    @Test
    void testEquals_SameInstance_ReturnsTrue() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();

        // Act & Assert
        assertEquals(entity, entity);
    }

    @Test
    void testEquals_NullObject_ReturnsFalse() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();

        // Act & Assert
        assertNotEquals(entity, null);
    }

    @Test
    void testHashCode_SameId_ReturnsSameHashCode() {
        // Arrange
        OAuthStateEntity entity1 = createTestEntity();
        OAuthStateEntity entity2 = createTestEntity();
        
        setId(entity1, 1L);
        setId(entity2, 1L);

        // Act & Assert
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testRedirectUri_CanBeNull() {
        // Arrange & Act
        OAuthStateEntity entity = new OAuthStateEntity(
            "token", "tenant", "nonce", null, Instant.now().plus(5, ChronoUnit.MINUTES)
        );

        // Assert
        assertNull(entity.getRedirectUri());
    }

    @Test
    void testStateToken_IsRequired() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();

        // Act & Assert
        assertNotNull(entity.getStateToken());
        assertFalse(entity.getStateToken().isEmpty());
    }

    @Test
    void testTenantSlug_IsRequired() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();

        // Act & Assert
        assertNotNull(entity.getTenantSlug());
        assertFalse(entity.getTenantSlug().isEmpty());
    }

    @Test
    void testNonce_IsRequired() {
        // Arrange
        OAuthStateEntity entity = createTestEntity();

        // Act & Assert
        assertNotNull(entity.getNonce());
        assertFalse(entity.getNonce().isEmpty());
    }

    // Helper methods

    private OAuthStateEntity createTestEntity() {
        return new OAuthStateEntity(
            "test-state-token",
            "test-tenant",
            "test-nonce",
            "https://example.com/callback",
            Instant.now().plus(5, ChronoUnit.MINUTES)
        );
    }

    private void setId(OAuthStateEntity entity, Long id) {
        try {
            var field = OAuthStateEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }
}
