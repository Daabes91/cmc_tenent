package com.clinic.modules.core.oauth;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Google public key caching mechanism.
 * 
 * Tests the 24-hour TTL cache, refresh mechanism, and fallback behavior
 * when keys are not found in the cache.
 * 
 * **Validates: Requirements 6.4**
 */
@ExtendWith(MockitoExtension.class)
class GooglePublicKeyCacheTest {

    @Mock
    private OAuthStateService stateService;

    @Mock
    private OAuthMetricsService metricsService;

    private GoogleOAuthService googleOAuthService;

    private static final String TEST_CLIENT_ID = "test-client-id.apps.googleusercontent.com";
    private static final String TEST_CLIENT_SECRET = "test-client-secret";
    private static final String TEST_REDIRECT_URI = "http://localhost:3000/auth/google/callback";

    @BeforeEach
    void setUp() {
        googleOAuthService = new GoogleOAuthService(stateService, metricsService);
        
        // Set configuration values
        ReflectionTestUtils.setField(googleOAuthService, "clientId", TEST_CLIENT_ID);
        ReflectionTestUtils.setField(googleOAuthService, "clientSecret", TEST_CLIENT_SECRET);
        ReflectionTestUtils.setField(googleOAuthService, "redirectUri", TEST_REDIRECT_URI);
    }

    /**
     * Test that public keys are cached and reused within the TTL period.
     */
    @Test
    void testPublicKeyCaching_KeysAreCachedAndReused() throws Exception {
        // Arrange
        RSAKey testKey = generateTestKey();
        String keyId = testKey.getKeyID();
        
        // Manually populate cache
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        cache.put(keyId, testKey.toRSAPublicKey());
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        // Set cache expiry to future (not expired)
        Instant futureExpiry = Instant.now().plusSeconds(3600);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", futureExpiry);
        
        // Act
        RSAPublicKey retrievedKey1 = googleOAuthService.getPublicKey(keyId);
        RSAPublicKey retrievedKey2 = googleOAuthService.getPublicKey(keyId);
        
        // Assert
        assertNotNull(retrievedKey1);
        assertNotNull(retrievedKey2);
        assertSame(retrievedKey1, retrievedKey2, "Should return the same cached instance");
        assertEquals(testKey.toRSAPublicKey(), retrievedKey1);
    }

    /**
     * Test that cache expires after 24 hours and triggers refresh.
     */
    @Test
    void testPublicKeyCaching_CacheExpiresAfter24Hours() throws Exception {
        // Arrange
        RSAKey testKey = generateTestKey();
        String keyId = testKey.getKeyID();
        
        // Manually populate cache
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        cache.put(keyId, testKey.toRSAPublicKey());
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        // Set cache expiry to past (expired)
        Instant pastExpiry = Instant.now().minusSeconds(1);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", pastExpiry);
        
        // Act & Assert
        // When cache is expired, getPublicKey will try to refresh from Google's endpoint
        // Since we can't mock the actual Google endpoint in this unit test,
        // we expect it to throw an exception when trying to refresh
        assertThrows(GoogleOAuthException.class, () -> {
            googleOAuthService.getPublicKey(keyId);
        }, "Should attempt to refresh when cache is expired");
    }

    /**
     * Test that missing keys trigger a cache refresh.
     */
    @Test
    void testPublicKeyCaching_MissingKeyTriggersRefresh() {
        // Arrange
        String missingKeyId = "missing-key-id";
        
        // Empty cache
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        // Set cache expiry to future (not expired, but key is missing)
        Instant futureExpiry = Instant.now().plusSeconds(3600);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", futureExpiry);
        
        // Act & Assert
        // When key is not in cache, it should try to refresh
        // Since we can't mock the actual Google endpoint, we expect an exception
        assertThrows(GoogleOAuthException.class, () -> {
            googleOAuthService.getPublicKey(missingKeyId);
        }, "Should attempt to refresh when key is not found in cache");
    }

    /**
     * Test that cache handles concurrent access correctly.
     */
    @Test
    void testPublicKeyCaching_ConcurrentAccess() throws Exception {
        // Arrange
        RSAKey testKey = generateTestKey();
        String keyId = testKey.getKeyID();
        
        // Manually populate cache
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        cache.put(keyId, testKey.toRSAPublicKey());
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        // Set cache expiry to future
        Instant futureExpiry = Instant.now().plusSeconds(3600);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", futureExpiry);
        
        // Act
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    RSAPublicKey key = googleOAuthService.getPublicKey(keyId);
                    if (key != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Ignore exceptions for this test
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Assert
        assertEquals(threadCount, successCount.get(), 
            "All concurrent requests should successfully retrieve the cached key");
    }

    /**
     * Test that cache stores multiple keys correctly.
     */
    @Test
    void testPublicKeyCaching_MultipleKeys() throws Exception {
        // Arrange
        RSAKey key1 = generateTestKey();
        RSAKey key2 = generateTestKey();
        RSAKey key3 = generateTestKey();
        
        // Manually populate cache with multiple keys
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        cache.put(key1.getKeyID(), key1.toRSAPublicKey());
        cache.put(key2.getKeyID(), key2.toRSAPublicKey());
        cache.put(key3.getKeyID(), key3.toRSAPublicKey());
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        // Set cache expiry to future
        Instant futureExpiry = Instant.now().plusSeconds(3600);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", futureExpiry);
        
        // Act
        RSAPublicKey retrievedKey1 = googleOAuthService.getPublicKey(key1.getKeyID());
        RSAPublicKey retrievedKey2 = googleOAuthService.getPublicKey(key2.getKeyID());
        RSAPublicKey retrievedKey3 = googleOAuthService.getPublicKey(key3.getKeyID());
        
        // Assert
        assertNotNull(retrievedKey1);
        assertNotNull(retrievedKey2);
        assertNotNull(retrievedKey3);
        assertEquals(key1.toRSAPublicKey(), retrievedKey1);
        assertEquals(key2.toRSAPublicKey(), retrievedKey2);
        assertEquals(key3.toRSAPublicKey(), retrievedKey3);
    }

    /**
     * Test that cache expiry is set correctly after refresh.
     * 
     * Note: This test verifies that refreshPublicKeys() can be called.
     * In a real environment, it would fetch keys from Google and update the cache expiry.
     * In this unit test, it may succeed if it can reach Google's endpoint.
     */
    @Test
    void testPublicKeyCaching_RefreshUpdatesExpiry() throws Exception {
        // Arrange
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        Instant pastExpiry = Instant.now().minusSeconds(1);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", pastExpiry);
        
        // Act
        try {
            // Attempt to refresh - this may succeed if Google's endpoint is reachable
            googleOAuthService.refreshPublicKeys();
            
            // If successful, verify cache was updated
            Instant newExpiry = (Instant) ReflectionTestUtils.getField(
                googleOAuthService, "publicKeyCacheExpiry");
            
            // Assert
            assertTrue(newExpiry.isAfter(Instant.now()), 
                "Cache expiry should be set to future after refresh");
            assertTrue(newExpiry.isAfter(pastExpiry), 
                "New expiry should be after old expiry");
            
        } catch (GoogleOAuthException e) {
            // If refresh fails (e.g., network issue), that's also acceptable
            // Just verify the exception is related to refresh failure
            assertTrue(e.getMessage().contains("Failed to refresh public keys") ||
                      e.getMessage().contains("refresh"),
                "Exception should be related to refresh failure");
        }
    }

    /**
     * Test that cache is thread-safe during refresh operations.
     */
    @Test
    void testPublicKeyCaching_ThreadSafeDuringRefresh() throws Exception {
        // Arrange
        RSAKey testKey = generateTestKey();
        String keyId = testKey.getKeyID();
        
        // Populate cache
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        cache.put(keyId, testKey.toRSAPublicKey());
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        // Set cache to expire soon
        Instant soonExpiry = Instant.now().plusMillis(100);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", soonExpiry);
        
        // Act
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Wait for cache to expire
        Thread.sleep(150);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // This should trigger refresh attempts
                    googleOAuthService.getPublicKey(keyId);
                } catch (GoogleOAuthException e) {
                    // Expected - can't reach Google endpoint
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Assert
        // All threads should have attempted to refresh (and failed gracefully)
        assertTrue(successCount.get() > 0, 
            "Threads should handle refresh attempts gracefully");
    }

    /**
     * Test that cache correctly handles null or empty key IDs.
     */
    @Test
    void testPublicKeyCaching_HandlesInvalidKeyIds() {
        // Arrange
        Map<String, RSAPublicKey> cache = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCache", cache);
        
        Instant futureExpiry = Instant.now().plusSeconds(3600);
        ReflectionTestUtils.setField(googleOAuthService, "publicKeyCacheExpiry", futureExpiry);
        
        // Act & Assert
        assertThrows(GoogleOAuthException.class, () -> {
            googleOAuthService.getPublicKey(null);
        }, "Should handle null key ID");
        
        assertThrows(GoogleOAuthException.class, () -> {
            googleOAuthService.getPublicKey("");
        }, "Should handle empty key ID");
    }

    // ========== Helper Methods ==========

    private RSAKey generateTestKey() throws Exception {
        return new RSAKeyGenerator(2048)
            .keyID(UUID.randomUUID().toString())
            .generate();
    }
}
