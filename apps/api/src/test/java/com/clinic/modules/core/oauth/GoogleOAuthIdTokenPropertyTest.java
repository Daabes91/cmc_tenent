package com.clinic.modules.core.oauth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Google OAuth ID token validation.
 * 
 * **Feature: patient-google-oauth, Property 16: ID token signature validation**
 * 
 * Tests that ID token signature validation works correctly across many different
 * token scenarios, including valid tokens, expired tokens, invalid signatures,
 * wrong issuers, and wrong audiences.
 */
class GoogleOAuthIdTokenPropertyTest {

    private static final String TEST_CLIENT_ID = "test-client-id.apps.googleusercontent.com";
    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String TEST_NONCE = "test-nonce";

    /**
     * Property: Valid ID tokens with correct signature, issuer, audience, and expiration
     * should always pass validation.
     * 
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 100)
    void validIdTokensShouldPassValidation(
        @ForAll("validGoogleIdTokenData") TokenData tokenData
    ) throws Exception {
        // Arrange
        GoogleOAuthService service = createServiceWithKey(tokenData.rsaKey());
        
        // Act
        boolean isValid = service.validateIdToken(tokenData.token().serialize(), TEST_NONCE);
        
        // Assert
        assertTrue(isValid, "Valid ID token should pass validation");
    }

    /**
     * Property: ID tokens with invalid signatures should always fail validation.
     * 
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 100)
    void idTokensWithInvalidSignaturesShouldFailValidation(
        @ForAll("validGoogleIdTokenData") TokenData validTokenData,
        @ForAll("rsaKey") RSAKey differentKey
    ) throws Exception {
        // Arrange
        GoogleOAuthService service = createServiceWithKey(validTokenData.rsaKey());
        
        // Create a token signed with a different key (invalid signature)
        JWTClaimsSet claims = validTokenData.token().getJWTClaimsSet();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(differentKey.getKeyID())
            .build();
        
        SignedJWT invalidToken = new SignedJWT(header, claims);
        JWSSigner signer = new RSASSASigner(differentKey);
        invalidToken.sign(signer);
        
        // Act & Assert
        assertThrows(GoogleOAuthException.class, () -> {
            service.validateIdToken(invalidToken.serialize(), TEST_NONCE);
        }, "ID token with invalid signature should fail validation");
    }

    /**
     * Property: Expired ID tokens should always fail validation.
     * 
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 100)
    void expiredIdTokensShouldFailValidation(
        @ForAll("expiredGoogleIdTokenData") TokenData expiredTokenData
    ) throws Exception {
        // Arrange
        GoogleOAuthService service = createServiceWithKey(expiredTokenData.rsaKey());
        
        // Act
        boolean isValid = service.validateIdToken(expiredTokenData.token().serialize(), TEST_NONCE);
        
        // Assert
        assertFalse(isValid, "Expired ID token should fail validation");
    }

    /**
     * Property: ID tokens with wrong issuer should always fail validation.
     * 
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 100)
    void idTokensWithWrongIssuerShouldFailValidation(
        @ForAll("idTokensWithWrongIssuerData") TokenData tokenData
    ) throws Exception {
        // Arrange
        GoogleOAuthService service = createServiceWithKey(tokenData.rsaKey());
        
        // Act
        boolean isValid = service.validateIdToken(tokenData.token().serialize(), TEST_NONCE);
        
        // Assert
        assertFalse(isValid, "ID token with wrong issuer should fail validation");
    }

    /**
     * Property: ID tokens with wrong audience should always fail validation.
     * 
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 100)
    void idTokensWithWrongAudienceShouldFailValidation(
        @ForAll("idTokensWithWrongAudienceData") TokenData tokenData
    ) throws Exception {
        // Arrange
        GoogleOAuthService service = createServiceWithKey(tokenData.rsaKey());
        
        // Act
        boolean isValid = service.validateIdToken(tokenData.token().serialize(), TEST_NONCE);
        
        // Assert
        assertFalse(isValid, "ID token with wrong audience should fail validation");
    }

    // ========== Providers ==========

    @Provide
    Arbitrary<TokenData> validGoogleIdTokenData() {
        return Arbitraries.randomValue(random -> {
            try {
                RSAKey rsaKey = generateRsaKey();
                SignedJWT token = createValidIdToken(rsaKey, TEST_CLIENT_ID, GOOGLE_ISSUER);
                return new TokenData(token, rsaKey);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate valid ID token", e);
            }
        });
    }

    @Provide
    Arbitrary<TokenData> expiredGoogleIdTokenData() {
        return Arbitraries.randomValue(random -> {
            try {
                RSAKey rsaKey = generateRsaKey();
                
                // Create token that expired 1 hour ago
                Instant expiredTime = Instant.now().minusSeconds(3600);
                
                JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(GOOGLE_ISSUER)
                    .audience(TEST_CLIENT_ID)
                    .subject(UUID.randomUUID().toString())
                    .expirationTime(Date.from(expiredTime))
                    .issueTime(Date.from(expiredTime.minusSeconds(3600)))
                    .claim("email", "test@example.com")
                    .claim("email_verified", true)
                    .claim("nonce", TEST_NONCE)
                    .build();
                
                SignedJWT token = signToken(rsaKey, claims);
                return new TokenData(token, rsaKey);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate expired ID token", e);
            }
        });
    }

    @Provide
    Arbitrary<TokenData> idTokensWithWrongIssuerData() {
        return Arbitraries.randomValue(random -> {
            try {
                RSAKey rsaKey = generateRsaKey();
                SignedJWT token = createValidIdToken(rsaKey, TEST_CLIENT_ID, "https://evil.com");
                return new TokenData(token, rsaKey);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate ID token with wrong issuer", e);
            }
        });
    }

    @Provide
    Arbitrary<TokenData> idTokensWithWrongAudienceData() {
        return Arbitraries.randomValue(random -> {
            try {
                RSAKey rsaKey = generateRsaKey();
                SignedJWT token = createValidIdToken(rsaKey, "wrong-client-id", GOOGLE_ISSUER);
                return new TokenData(token, rsaKey);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate ID token with wrong audience", e);
            }
        });
    }

    @Provide
    Arbitrary<RSAKey> rsaKey() {
        return Arbitraries.randomValue(random -> {
            try {
                return generateRsaKey();
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate RSA key", e);
            }
        });
    }

    // ========== Helper Methods ==========

    private RSAKey generateRsaKey() throws Exception {
        return new RSAKeyGenerator(2048)
            .keyID(UUID.randomUUID().toString())
            .generate();
    }

    private SignedJWT createValidIdToken(RSAKey rsaKey, String audience, String issuer) throws Exception {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(3600); // Valid for 1 hour
        
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(issuer)
            .audience(audience)
            .subject(UUID.randomUUID().toString())
            .expirationTime(Date.from(expiration))
            .issueTime(Date.from(now))
            .claim("email", "test@example.com")
            .claim("email_verified", true)
            .claim("name", "Test User")
            .claim("nonce", TEST_NONCE)
            .build();
        
        return signToken(rsaKey, claims);
    }

    private SignedJWT signToken(RSAKey rsaKey, JWTClaimsSet claims) throws Exception {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.getKeyID())
            .build();
        
        SignedJWT signedJWT = new SignedJWT(header, claims);
        JWSSigner signer = new RSASSASigner(rsaKey);
        signedJWT.sign(signer);
        
        return signedJWT;
    }

    private GoogleOAuthService createServiceWithKey(RSAKey rsaKey) throws Exception {
        OAuthStateService mockStateService = new OAuthStateService(
            new InMemoryOAuthStateRepository()
        );
        
        OAuthMetricsService mockMetricsService = Mockito.mock(OAuthMetricsService.class);
        
        GoogleOAuthService service = new GoogleOAuthService(mockStateService, mockMetricsService);
        
        // Set configuration
        ReflectionTestUtils.setField(service, "clientId", TEST_CLIENT_ID);
        ReflectionTestUtils.setField(service, "clientSecret", "test-secret");
        ReflectionTestUtils.setField(service, "redirectUri", "http://localhost/callback");
        
        // Inject the public key into the service's cache
        Map<String, RSAPublicKey> publicKeyCache = new ConcurrentHashMap<>();
        publicKeyCache.put(rsaKey.getKeyID(), rsaKey.toRSAPublicKey());
        
        ReflectionTestUtils.setField(service, "publicKeyCache", publicKeyCache);
        
        // Set cache expiry to future
        ReflectionTestUtils.setField(service, "publicKeyCacheExpiry", 
            Instant.now().plusSeconds(86400));
        
        return service;
    }

    /**
     * Helper record to bundle token with its signing key
     */
    private record TokenData(SignedJWT token, RSAKey rsaKey) {}
}
