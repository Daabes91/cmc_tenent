package com.clinic.modules.admin.auth;

import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private static final int TOKEN_EXPIRATION_HOURS = 1;
    private static final int TOKEN_LENGTH_BYTES = 32;
    private static final int RATE_LIMIT_MINUTES = 5;

    private final PasswordResetTokenRepository tokenRepository;
    private final StaffUserRepository staffUserRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;
    private final SecureRandom secureRandom;
    
    // In-memory cache for rate limiting (email -> last request timestamp)
    private final ConcurrentHashMap<String, Instant> rateLimitCache;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            StaffUserRepository staffUserRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetEmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.staffUserRepository = staffUserRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.secureRandom = new SecureRandom();
        this.rateLimitCache = new ConcurrentHashMap<>();
    }

    /**
     * Request a password reset for a staff member
     * Sends an email with the reset link
     * 
     * @param email Staff member's email
     * @param tenantSlug Tenant slug
     * @param language Language code (en or ar)
     */
    @Transactional
    public void requestPasswordReset(String email, String tenantSlug, String language) {
        // Check rate limiting
        String rateLimitKey = email.toLowerCase() + ":" + tenantSlug.toLowerCase();
        Instant lastRequest = rateLimitCache.get(rateLimitKey);
        Instant now = Instant.now();
        
        if (lastRequest != null && now.isBefore(lastRequest.plus(RATE_LIMIT_MINUTES, ChronoUnit.MINUTES))) {
            // Rate limited - return silently to prevent spam
            return;
        }

        // Find tenant
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(tenantSlug);
        if (tenantOpt.isEmpty()) {
            // Return silently to prevent user enumeration
            return;
        }
        TenantEntity tenant = tenantOpt.get();

        // Find staff member
        Optional<StaffUser> staffOpt = staffUserRepository.findByEmailIgnoreCaseAndTenantId(email, tenant.getId());
        if (staffOpt.isEmpty()) {
            // Return silently to prevent user enumeration
            return;
        }
        StaffUser staff = staffOpt.get();

        // Update rate limit cache
        rateLimitCache.put(rateLimitKey, now);

        // Invalidate existing tokens for this staff member
        invalidateExistingTokens(staff.getId());
        tokenRepository.flush(); // Ensure invalidation is persisted before creating new token

        // Generate new token
        String plainToken = generateSecureToken();
        String tokenHash = passwordEncoder.encode(plainToken);
        
        Instant expiresAt = now.plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS);
        
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity(
            tokenHash,
            staff,
            tenant,
            expiresAt
        );
        
        tokenRepository.save(tokenEntity);
        
        // Send email with reset link
        String clinicName = tenant.getName();
        emailService.sendPasswordResetEmail(email, plainToken, clinicName, tenantSlug, language);
    }

    /**
     * Validate a reset token
     * 
     * @param plainToken The plain token from the reset link
     * @return true if token is valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateResetToken(String plainToken) {
        return findValidToken(plainToken).isPresent();
    }

    /**
     * Validate a reset token and return tenant information
     * 
     * @param plainToken The plain token from the reset link
     * @return Optional containing validation result with tenant slug if valid, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<TokenValidationResult> validateResetTokenWithTenant(String plainToken) {
        return findValidToken(plainToken)
            .map(token -> new TokenValidationResult(token.getTenant().getSlug()));
    }

    /**
     * Result of token validation including tenant information
     */
    public record TokenValidationResult(String tenantSlug) {}

    /**
     * Reset password using a valid token
     * 
     * @param plainToken The plain token from the reset link
     * @param newPassword The new password
     * @return true if password was reset successfully, false otherwise
     */
    @Transactional
    public boolean resetPassword(String plainToken, String newPassword) {
        Optional<PasswordResetTokenEntity> tokenOpt = findValidToken(plainToken);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetTokenEntity token = tokenOpt.get();
        StaffUser staff = token.getStaff();
        
        // Update password
        String newPasswordHash = passwordEncoder.encode(newPassword);
        staff.setPasswordHash(newPasswordHash);
        staffUserRepository.save(staff);
        
        // Mark token as used
        token.markAsUsed();
        tokenRepository.save(token);
        
        return true;
    }

    /**
     * Invalidate all existing unused tokens for a staff member
     * 
     * @param staffId Staff member ID
     */
    @Transactional
    public void invalidateExistingTokens(Long staffId) {
        // Fetch and update tokens individually to ensure they're in the current persistence context
        List<PasswordResetTokenEntity> unusedTokens = tokenRepository.findByStaffIdAndUsedFalse(staffId);
        Instant now = Instant.now();
        for (PasswordResetTokenEntity token : unusedTokens) {
            token.setUsed(true);
            token.setUsedAt(now);
        }
        tokenRepository.saveAll(unusedTokens);
    }

    /**
     * Find a valid token by matching the plain token against stored hashes
     * A valid token must:
     * - Exist in the database
     * - Not be expired
     * - Not be used
     * - Match the provided plain token when hashed
     */
    private Optional<PasswordResetTokenEntity> findValidToken(String plainToken) {
        // We need to check all tokens because BCrypt hashing is one-way
        // In practice, we could optimize this by adding an index or using a different approach
        // For now, we'll fetch recent unused tokens and check them
        Instant cutoff = Instant.now().minus(TOKEN_EXPIRATION_HOURS + 1, ChronoUnit.HOURS);
        
        return tokenRepository.findAll().stream()
            .filter(token -> !token.isUsed())
            .filter(token -> !token.isExpired())
            .filter(token -> token.getCreatedAt().isAfter(cutoff))
            .filter(token -> passwordEncoder.matches(plainToken, token.getTokenHash()))
            .findFirst();
    }

    /**
     * Generate a cryptographically secure random token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Clear rate limit cache entry (useful for testing)
     */
    public void clearRateLimit(String email, String tenantSlug) {
        String rateLimitKey = email.toLowerCase() + ":" + tenantSlug.toLowerCase();
        rateLimitCache.remove(rateLimitKey);
    }

    /**
     * Request a password reset for testing purposes (returns the token instead of sending email)
     * This method is package-private and should only be used in tests
     * 
     * @param email Staff member's email
     * @param tenantSlug Tenant slug
     * @return Plain token, or empty if rate limited or user not found
     */
    @Transactional
    Optional<String> requestPasswordResetForTesting(String email, String tenantSlug) {
        // Check rate limiting
        String rateLimitKey = email.toLowerCase() + ":" + tenantSlug.toLowerCase();
        Instant lastRequest = rateLimitCache.get(rateLimitKey);
        Instant now = Instant.now();
        
        if (lastRequest != null && now.isBefore(lastRequest.plus(RATE_LIMIT_MINUTES, ChronoUnit.MINUTES))) {
            // Rate limited - return empty to prevent spam
            return Optional.empty();
        }

        // Find tenant
        Optional<TenantEntity> tenantOpt = tenantRepository.findBySlugIgnoreCaseAndNotDeleted(tenantSlug);
        if (tenantOpt.isEmpty()) {
            return Optional.empty();
        }
        TenantEntity tenant = tenantOpt.get();

        // Find staff member
        Optional<StaffUser> staffOpt = staffUserRepository.findByEmailIgnoreCaseAndTenantId(email, tenant.getId());
        if (staffOpt.isEmpty()) {
            return Optional.empty();
        }
        StaffUser staff = staffOpt.get();

        // Update rate limit cache
        rateLimitCache.put(rateLimitKey, now);

        // Invalidate existing tokens for this staff member
        invalidateExistingTokens(staff.getId());
        tokenRepository.flush(); // Ensure invalidation is persisted before creating new token

        // Generate new token
        String plainToken = generateSecureToken();
        String tokenHash = passwordEncoder.encode(plainToken);
        
        Instant expiresAt = now.plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS);
        
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity(
            tokenHash,
            staff,
            tenant,
            expiresAt
        );
        
        tokenRepository.save(tokenEntity);
        
        return Optional.of(plainToken);
    }
}
