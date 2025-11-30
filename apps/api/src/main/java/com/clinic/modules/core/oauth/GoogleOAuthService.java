package com.clinic.modules.core.oauth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.nimbusds.jose.JOSEException;
import io.micrometer.core.instrument.Timer;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling Google OAuth 2.0 operations.
 * Manages authorization URL generation, token exchange, ID token validation,
 * and user info retrieval.
 */
@Service
public class GoogleOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthService.class);
    
    private static final String GOOGLE_AUTH_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final List<String> SCOPES = Arrays.asList(
        "openid",
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile"
    );
    
    // Cache for Google's public keys (24-hour TTL)
    private final Map<String, RSAPublicKey> publicKeyCache = new ConcurrentHashMap<>();
    private Instant publicKeyCacheExpiry = Instant.now();
    private static final long CACHE_TTL_HOURS = 24;

    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final OAuthStateService stateService;
    private final OAuthMetricsService metricsService;

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    public GoogleOAuthService(OAuthStateService stateService, OAuthMetricsService metricsService) {
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();
        this.stateService = stateService;
        this.metricsService = metricsService;
    }

    /**
     * Generate Google OAuth authorization URL with tenant context.
     * 
     * @param tenantSlug The tenant identifier to maintain context
     * @param redirectUri The callback URL for OAuth redirect
     * @return The authorization URL to redirect the user to
     */
    public String generateAuthorizationUrl(String tenantSlug, String redirectUri) {
        try {
            // Generate nonce for additional security
            String nonce = generateNonce();
            
            // Generate and store state token with tenant context
            String state = stateService.generateState(tenantSlug, nonce, redirectUri);
            
            // Build authorization URL
            AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                httpTransport,
                jsonFactory,
                new GenericUrl(GOOGLE_TOKEN_URI),
                new ClientParametersAuthentication(clientId, clientSecret),
                clientId,
                GOOGLE_AUTH_URI
            )
            .setScopes(SCOPES)
            .build();
            
            String authUrl = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(state)
                .set("nonce", nonce)
                .set("access_type", "offline")
                .set("prompt", "consent")
                .build();
            
            logger.info("Generated authorization URL for tenant: {}", tenantSlug);
            return authUrl;
            
        } catch (Exception e) {
            logger.error("Failed to generate authorization URL for tenant: {}", tenantSlug, e);
            throw new GoogleOAuthException("Failed to generate authorization URL", e);
        }
    }

    /**
     * Exchange authorization code for access token.
     * 
     * @param code The authorization code from Google
     * @param redirectUri The redirect URI used in the authorization request
     * @return GoogleTokenResponse containing access token and ID token
     */
    public GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri) {
        Timer.Sample sample = metricsService.startGoogleApiTimer();
        metricsService.recordGoogleApiCall("token_exchange");
        
        try {
            AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                httpTransport,
                jsonFactory,
                new GenericUrl(GOOGLE_TOKEN_URI),
                new ClientParametersAuthentication(clientId, clientSecret),
                clientId,
                GOOGLE_AUTH_URI
            )
            .build();
            
            TokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();
            
            logger.info("Successfully exchanged authorization code for tokens");
            metricsService.recordGoogleApiSuccess("token_exchange");
            metricsService.stopGoogleApiTimer(sample, "token_exchange");
            
            return new GoogleTokenResponse(
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                (String) tokenResponse.get("id_token"),
                tokenResponse.getExpiresInSeconds().intValue(),
                tokenResponse.getTokenType()
            );
            
        } catch (IOException e) {
            logger.error("Failed to exchange authorization code for token", e);
            metricsService.recordGoogleApiError("token_exchange", "io_exception");
            metricsService.recordNetworkError("token_exchange");
            throw new GoogleOAuthException("Failed to exchange authorization code", e);
        }
    }

    /**
     * Validate ID token signature using Google's public keys.
     * 
     * @param idToken The ID token to validate
     * @return true if the signature is valid
     * @throws GoogleOAuthException if validation fails
     */
    public boolean validateIdToken(String idToken, String expectedNonce) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            
            // Get the key ID from the token header
            String keyId = signedJWT.getHeader().getKeyID();
            
            // Get the public key for this key ID
            RSAPublicKey publicKey = getPublicKey(keyId);
            
            // Verify the signature
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            boolean signatureValid = signedJWT.verify(verifier);
            
            if (!signatureValid) {
                logger.error("ID token signature validation failed");
                return false;
            }
            
            // Validate claims
            Map<String, Object> claims = signedJWT.getJWTClaimsSet().getClaims();
            
            // Validate issuer
            String issuer = (String) claims.get("iss");
            if (!issuer.equals("https://accounts.google.com") && !issuer.equals("accounts.google.com")) {
                logger.error("Invalid issuer in ID token: {}", issuer);
                return false;
            }
            
            // Validate audience (can be String or List)
            Object audObj = claims.get("aud");
            boolean audienceValid = false;
            if (audObj instanceof String) {
                audienceValid = audObj.equals(clientId);
            } else if (audObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> audiences = (java.util.List<String>) audObj;
                audienceValid = audiences.contains(clientId);
            }
            
            if (!audienceValid) {
                logger.error("Invalid audience in ID token: {}", audObj);
                return false;
            }
            
            // Validate expiration
            Date expiration = (Date) claims.get("exp");
            if (expiration.before(new Date())) {
                logger.error("ID token has expired");
                return false;
            }

            // Validate nonce to prevent token replay
            String tokenNonce = (String) claims.get("nonce");
            if (expectedNonce == null || expectedNonce.isBlank()) {
                logger.error("Expected nonce missing while validating ID token");
                return false;
            }
            if (tokenNonce == null || !expectedNonce.equals(tokenNonce)) {
                logger.error("Nonce validation failed: expected {}, got {}", expectedNonce, tokenNonce);
                return false;
            }
            
            logger.info("ID token validation successful");
            return true;
            
        } catch (ParseException | JOSEException e) {
            logger.error("Failed to validate ID token", e);
            throw new GoogleOAuthException("Failed to validate ID token", e);
        }
    }

    /**
     * Retrieve user info from Google UserInfo API.
     * 
     * @param accessToken The access token from token exchange
     * @return GoogleUserInfo containing user profile data
     */
    public GoogleUserInfo getUserInfo(String accessToken) {
        Timer.Sample sample = metricsService.startGoogleApiTimer();
        metricsService.recordGoogleApiCall("userinfo");
        
        try {
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(
                request -> request.setParser(new JsonObjectParser(jsonFactory))
            );
            
            GenericUrl url = new GenericUrl(GOOGLE_USERINFO_URI);
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().setAuthorization("Bearer " + accessToken);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = request.execute().parseAs(Map.class);
            
            logger.info("Successfully retrieved user info from Google");
            metricsService.recordGoogleApiSuccess("userinfo");
            metricsService.stopGoogleApiTimer(sample, "userinfo");
            
            return new GoogleUserInfo(
                (String) response.get("sub"),
                (String) response.get("email"),
                Boolean.TRUE.equals(response.get("email_verified")),
                (String) response.get("name"),
                (String) response.get("given_name"),
                (String) response.get("family_name"),
                (String) response.get("picture"),
                (String) response.get("locale")
            );
            
        } catch (IOException e) {
            logger.error("Failed to retrieve user info from Google", e);
            metricsService.recordGoogleApiError("userinfo", "io_exception");
            metricsService.recordNetworkError("userinfo");
            throw new GoogleOAuthException("Failed to retrieve user info", e);
        }
    }

    /**
     * Get Google's public key for the given key ID.
     * Uses caching with 24-hour TTL.
     * Package-private for testing.
     * 
     * @param keyId The key ID from the JWT header
     * @return The RSA public key
     */
    RSAPublicKey getPublicKey(String keyId) {
        try {
            // Check if cache is expired
            if (Instant.now().isAfter(publicKeyCacheExpiry)) {
                logger.info("Public key cache expired, refreshing...");
                refreshPublicKeys();
            }
            
            // Try to get from cache
            RSAPublicKey publicKey = publicKeyCache.get(keyId);
            
            if (publicKey == null) {
                // Key not in cache, refresh and try again
                logger.info("Public key not found in cache, refreshing...");
                refreshPublicKeys();
                publicKey = publicKeyCache.get(keyId);
                
                if (publicKey == null) {
                    throw new GoogleOAuthException("Public key not found for key ID: " + keyId);
                }
            }
            
            return publicKey;
            
        } catch (Exception e) {
            logger.error("Failed to get public key for key ID: {}", keyId, e);
            throw new GoogleOAuthException("Failed to get public key", e);
        }
    }

    /**
     * Refresh Google's public keys from the JWKS endpoint.
     * Package-private for testing.
     */
    void refreshPublicKeys() {
        try {
            JWKSet jwkSet = JWKSet.load(new URL(GOOGLE_JWKS_URI));
            
            publicKeyCache.clear();
            
            for (JWK jwk : jwkSet.getKeys()) {
                if (jwk instanceof RSAKey) {
                    RSAKey rsaKey = (RSAKey) jwk;
                    publicKeyCache.put(rsaKey.getKeyID(), rsaKey.toRSAPublicKey());
                }
            }
            
            // Update cache expiry
            publicKeyCacheExpiry = Instant.now().plusSeconds(CACHE_TTL_HOURS * 3600);
            
            logger.info("Refreshed {} public keys from Google", publicKeyCache.size());
            
        } catch (Exception e) {
            logger.error("Failed to refresh public keys from Google", e);
            throw new GoogleOAuthException("Failed to refresh public keys", e);
        }
    }

    /**
     * Generate a cryptographically secure nonce.
     * 
     * @return A random nonce string
     */
    private String generateNonce() {
        return java.util.UUID.randomUUID().toString();
    }
}
