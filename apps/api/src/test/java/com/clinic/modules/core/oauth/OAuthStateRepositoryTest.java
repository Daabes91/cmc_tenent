package com.clinic.modules.core.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OAuthStateRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Transactional
class OAuthStateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OAuthStateRepository repository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        repository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindByStateToken_ExistingToken_ReturnsEntity() {
        // Arrange
        String stateToken = "test-token-123";
        OAuthStateEntity entity = createAndPersistEntity(stateToken, "tenant1", 5);

        // Act
        Optional<OAuthStateEntity> result = repository.findByStateToken(stateToken);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(stateToken, result.get().getStateToken());
        assertEquals("tenant1", result.get().getTenantSlug());
    }

    @Test
    void testFindByStateToken_NonExistingToken_ReturnsEmpty() {
        // Arrange
        createAndPersistEntity("token1", "tenant1", 5);

        // Act
        Optional<OAuthStateEntity> result = repository.findByStateToken("non-existing-token");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteExpiredStates_DeletesOnlyExpiredStates() {
        // Arrange
        Instant now = Instant.now();
        
        // Create expired states (expired 1 minute ago)
        createAndPersistEntity("expired1", "tenant1", -1);
        createAndPersistEntity("expired2", "tenant2", -1);
        
        // Create valid states (expires in 5 minutes)
        createAndPersistEntity("valid1", "tenant3", 5);
        createAndPersistEntity("valid2", "tenant4", 5);
        
        entityManager.flush();
        entityManager.clear();

        // Act
        int deletedCount = repository.deleteExpiredStates(now);
        entityManager.flush();

        // Assert
        assertEquals(2, deletedCount);
        assertEquals(2, repository.count());
        
        // Verify valid states still exist
        assertTrue(repository.findByStateToken("valid1").isPresent());
        assertTrue(repository.findByStateToken("valid2").isPresent());
        
        // Verify expired states are gone
        assertFalse(repository.findByStateToken("expired1").isPresent());
        assertFalse(repository.findByStateToken("expired2").isPresent());
    }

    @Test
    void testDeleteExpiredStates_NoExpiredStates_DeletesNothing() {
        // Arrange
        createAndPersistEntity("valid1", "tenant1", 5);
        createAndPersistEntity("valid2", "tenant2", 5);
        entityManager.flush();

        // Act
        int deletedCount = repository.deleteExpiredStates(Instant.now());
        entityManager.flush();

        // Assert
        assertEquals(0, deletedCount);
        assertEquals(2, repository.count());
    }

    @Test
    void testDeleteConsumedStates_DeletesOnlyOldConsumedStates() {
        // Arrange
        Instant now = Instant.now();
        Instant cutoffTime = now.minus(1, ChronoUnit.HOURS);
        
        // Create old consumed state (created 2 hours ago)
        OAuthStateEntity oldConsumed = createEntityWithCreatedAt(
            "old-consumed", "tenant1", 5, now.minus(2, ChronoUnit.HOURS)
        );
        oldConsumed.markConsumed();
        entityManager.persist(oldConsumed);
        
        // Create recent consumed state (created 30 minutes ago)
        OAuthStateEntity recentConsumed = createEntityWithCreatedAt(
            "recent-consumed", "tenant2", 5, now.minus(30, ChronoUnit.MINUTES)
        );
        recentConsumed.markConsumed();
        entityManager.persist(recentConsumed);
        
        // Create unconsumed state
        createAndPersistEntity("unconsumed", "tenant3", 5);
        
        entityManager.flush();
        entityManager.clear();

        // Act
        int deletedCount = repository.deleteConsumedStates(cutoffTime);
        entityManager.flush();

        // Assert
        assertEquals(1, deletedCount);
        assertEquals(2, repository.count());
        
        // Verify old consumed state is gone
        assertFalse(repository.findByStateToken("old-consumed").isPresent());
        
        // Verify recent consumed and unconsumed states still exist
        assertTrue(repository.findByStateToken("recent-consumed").isPresent());
        assertTrue(repository.findByStateToken("unconsumed").isPresent());
    }

    @Test
    void testDeleteConsumedStates_NoOldConsumedStates_DeletesNothing() {
        // Arrange
        Instant now = Instant.now();
        Instant cutoffTime = now.minus(1, ChronoUnit.HOURS);
        
        // Create recent consumed state
        OAuthStateEntity recentConsumed = createEntityWithCreatedAt(
            "recent", "tenant1", 5, now.minus(30, ChronoUnit.MINUTES)
        );
        recentConsumed.markConsumed();
        entityManager.persist(recentConsumed);
        
        entityManager.flush();

        // Act
        int deletedCount = repository.deleteConsumedStates(cutoffTime);
        entityManager.flush();

        // Assert
        assertEquals(0, deletedCount);
        assertEquals(1, repository.count());
    }

    @Test
    void testSaveAndRetrieve_PreservesAllFields() {
        // Arrange
        String stateToken = "test-token";
        String tenantSlug = "test-tenant";
        String nonce = "test-nonce";
        String redirectUri = "https://example.com/callback";
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);
        
        OAuthStateEntity entity = new OAuthStateEntity(
            stateToken, tenantSlug, nonce, redirectUri, expiresAt
        );

        // Act
        OAuthStateEntity saved = repository.save(entity);
        entityManager.flush();
        entityManager.clear();
        
        Optional<OAuthStateEntity> retrieved = repository.findByStateToken(stateToken);

        // Assert
        assertTrue(retrieved.isPresent());
        OAuthStateEntity result = retrieved.get();
        
        assertNotNull(result.getId());
        assertEquals(stateToken, result.getStateToken());
        assertEquals(tenantSlug, result.getTenantSlug());
        assertEquals(nonce, result.getNonce());
        assertEquals(redirectUri, result.getRedirectUri());
        assertNotNull(result.getCreatedAt());
        assertEquals(expiresAt.truncatedTo(ChronoUnit.MILLIS), 
                     result.getExpiresAt().truncatedTo(ChronoUnit.MILLIS));
        assertFalse(result.isConsumed());
    }

    @Test
    void testMarkConsumedAndSave_PersistsConsumedState() {
        // Arrange
        OAuthStateEntity entity = createAndPersistEntity("token", "tenant", 5);
        entityManager.flush();
        entityManager.clear();

        // Act
        OAuthStateEntity retrieved = repository.findByStateToken("token").orElseThrow();
        retrieved.markConsumed();
        repository.save(retrieved);
        entityManager.flush();
        entityManager.clear();

        // Assert
        OAuthStateEntity result = repository.findByStateToken("token").orElseThrow();
        assertTrue(result.isConsumed());
    }

    @Test
    void testUniqueConstraint_StateToken_PreventsDuplicates() {
        // Arrange
        String duplicateToken = "duplicate-token";
        createAndPersistEntity(duplicateToken, "tenant1", 5);
        entityManager.flush();

        // Act & Assert
        OAuthStateEntity duplicate = new OAuthStateEntity(
            duplicateToken, "tenant2", "nonce2", "uri2",
            Instant.now().plus(5, ChronoUnit.MINUTES)
        );
        
        assertThrows(Exception.class, () -> {
            repository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void testMultipleTenants_CanHaveDifferentStates() {
        // Arrange & Act
        createAndPersistEntity("token1", "tenant1", 5);
        createAndPersistEntity("token2", "tenant2", 5);
        createAndPersistEntity("token3", "tenant1", 5);
        entityManager.flush();

        // Assert
        assertEquals(3, repository.count());
        assertTrue(repository.findByStateToken("token1").isPresent());
        assertTrue(repository.findByStateToken("token2").isPresent());
        assertTrue(repository.findByStateToken("token3").isPresent());
    }

    // Helper methods

    private OAuthStateEntity createAndPersistEntity(String stateToken, String tenantSlug, int expiresInMinutes) {
        Instant expiresAt = Instant.now().plus(expiresInMinutes, ChronoUnit.MINUTES);
        OAuthStateEntity entity = new OAuthStateEntity(
            stateToken,
            tenantSlug,
            "nonce-" + stateToken,
            "https://example.com/callback",
            expiresAt
        );
        return entityManager.persist(entity);
    }

    private OAuthStateEntity createEntityWithCreatedAt(
            String stateToken, String tenantSlug, int expiresInMinutes, Instant createdAt) {
        Instant expiresAt = createdAt.plus(expiresInMinutes, ChronoUnit.MINUTES);
        OAuthStateEntity entity = new OAuthStateEntity(
            stateToken,
            tenantSlug,
            "nonce-" + stateToken,
            "https://example.com/callback",
            expiresAt
        );
        
        // Manually set createdAt using reflection to simulate old entities
        try {
            var field = OAuthStateEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(entity, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt", e);
        }
        
        return entity;
    }
}
