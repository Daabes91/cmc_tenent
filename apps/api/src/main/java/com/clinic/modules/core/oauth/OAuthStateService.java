package com.clinic.modules.core.oauth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for managing OAuth state tokens during the Google OAuth flow.
 * Handles generation, storage, validation, and consumption of state tokens.
 * 
 * State tokens are used to:
 * 1. Maintain tenant context during OAuth redirects
 * 2. Prevent CSRF attacks
 * 3. Ensure one-time use of authorization codes
 */
@Service
public class OAuthStateService {

    private static final int STATE_TOKEN_LENGTH = 32; // 32 bytes = 256 bits
    private static final int STATE_EXPIRATION_MINUTES = 5;
    
    private final OAuthStateRepository repository;
    private final SecureRandom secureRandom;

    public OAuthStateService(OAuthStateRepository repository) {
        this.repository = repository;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generate a cryptographically secure state token and store it with tenant context.
     * 
     * @param tenantSlug The tenant identifier to associate with this state
     * @param nonce A unique nonce for additional security
     * @param redirectUri Optional redirect URI to store with the state
     * @return The generated state token
     */
    @Transactional
    public String generateState(String tenantSlug, String nonce, String redirectUri) {
        if (tenantSlug == null || tenantSlug.isBlank()) {
            throw new IllegalArgumentException("Tenant slug cannot be null or empty");
        }
        if (nonce == null || nonce.isBlank()) {
            throw new IllegalArgumentException("Nonce cannot be null or empty");
        }

        // Generate cryptographically secure random token
        String stateToken = generateSecureToken();
        
        // Calculate expiration time
        Instant expiresAt = Instant.now().plus(STATE_EXPIRATION_MINUTES, ChronoUnit.MINUTES);
        
        // Create and save the state entity
        OAuthStateEntity stateEntity = new OAuthStateEntity(
            stateToken,
            tenantSlug,
            nonce,
            redirectUri,
            expiresAt
        );
        
        repository.save(stateEntity);
        
        return stateToken;
    }

    /**
     * Generate a cryptographically secure state token without redirect URI.
     * 
     * @param tenantSlug The tenant identifier to associate with this state
     * @param nonce A unique nonce for additional security
     * @return The generated state token
     */
    @Transactional
    public String generateState(String tenantSlug, String nonce) {
        return generateState(tenantSlug, nonce, null);
    }

    /**
     * Validate and consume a state token (one-time use).
     * This method ensures that:
     * 1. The state token exists
     * 2. The state has not expired
     * 3. The state has not been consumed already
     * 4. The state is marked as consumed after validation
     * 
     * @param stateToken The state token to validate
     * @return OAuthStateData containing tenant slug and nonce if valid
     * @throws InvalidOAuthStateException if the state is invalid, expired, or already consumed
     */
    @Transactional
    public OAuthStateData validateAndConsumeState(String stateToken) {
        if (stateToken == null || stateToken.isBlank()) {
            throw new InvalidOAuthStateException("State token cannot be null or empty");
        }

        Optional<OAuthStateEntity> stateOpt = repository.findByStateToken(stateToken);
        
        if (stateOpt.isEmpty()) {
            throw new InvalidOAuthStateException("Invalid state token");
        }

        OAuthStateEntity state = stateOpt.get();

        // Check if already consumed
        if (state.isConsumed()) {
            throw new InvalidOAuthStateException("State token has already been used");
        }

        // Check if expired
        if (state.isExpired()) {
            throw new InvalidOAuthStateException("State token has expired");
        }

        // Mark as consumed (one-time use)
        state.markConsumed();
        repository.save(state);

        return new OAuthStateData(
            state.getTenantSlug(),
            state.getNonce(),
            state.getRedirectUri()
        );
    }

    /**
     * Generate a cryptographically secure random token.
     * Uses SecureRandom for cryptographic strength and Base64 URL-safe encoding.
     * 
     * @return A URL-safe Base64 encoded random token
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[STATE_TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Clean up expired states.
     * This method should be called periodically by a scheduled job.
     * 
     * @return The number of expired states deleted
     */
    @Transactional
    public int cleanupExpiredStates() {
        return repository.deleteExpiredStates(Instant.now());
    }

    /**
     * Clean up consumed states older than the specified hours.
     * 
     * @param hoursOld Delete consumed states older than this many hours
     * @return The number of consumed states deleted
     */
    @Transactional
    public int cleanupConsumedStates(int hoursOld) {
        Instant cutoffTime = Instant.now().minus(hoursOld, ChronoUnit.HOURS);
        return repository.deleteConsumedStates(cutoffTime);
    }

    /**
     * Data class for returning validated OAuth state information
     */
    public record OAuthStateData(
        String tenantSlug,
        String nonce,
        String redirectUri
    ) {}
}
