package com.clinic.modules.saas.service;

import com.clinic.modules.saas.dto.SaasLoginRequest;
import com.clinic.modules.saas.dto.SaasLoginResponse;
import com.clinic.modules.saas.exception.UnauthorizedException;
import com.clinic.modules.saas.model.SaasManager;
import com.clinic.modules.saas.model.SaasManagerStatus;
import com.clinic.modules.saas.repository.SaasManagerRepository;
import com.clinic.security.SaasManagerJwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for SAAS Manager authentication operations
 */
@Service
public class SaasManagerService {

    private static final Logger log = LoggerFactory.getLogger(SaasManagerService.class);

    private final SaasManagerRepository saasManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SaasManagerJwtTokenService jwtTokenService;

    public SaasManagerService(
            SaasManagerRepository saasManagerRepository,
            PasswordEncoder passwordEncoder,
            SaasManagerJwtTokenService jwtTokenService
    ) {
        this.saasManagerRepository = saasManagerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Authenticate a SAAS Manager with email and password
     *
     * @param request Login request containing email and password
     * @return SaasLoginResponse with JWT token and manager details
     * @throws UnauthorizedException if credentials are invalid or account is not active
     */
    @Transactional(readOnly = true)
    public SaasLoginResponse authenticate(SaasLoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        log.info("SAAS Manager authentication attempt - email: {}", normalizedEmail);

        // Find manager by email
        SaasManager manager = getSaasManagerByEmail(normalizedEmail);

        // Check if account is active
        if (manager.getStatus() != SaasManagerStatus.ACTIVE) {
            log.warn("SAAS Manager authentication failed - account status: {} - email: {}", 
                    manager.getStatus(), normalizedEmail);
            throw new UnauthorizedException("Account is not active");
        }

        // Validate password
        if (!passwordEncoder.matches(request.password(), manager.getPasswordHash())) {
            log.warn("SAAS Manager authentication failed - invalid credentials - email: {}", normalizedEmail);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Generate JWT token
        var issuedToken = jwtTokenService.generateToken(manager);

        log.info("SAAS Manager authentication successful - managerId: {}, email: {}, name: {}, tokenExpiresAt: {}", 
                manager.getId(), normalizedEmail, manager.getFullName(), issuedToken.expiresAt());

        return SaasLoginResponse.bearer(
                issuedToken.token(),
                issuedToken.expiresAt(),
                manager.getId(),
                manager.getEmail(),
                manager.getFullName()
        );
    }

    /**
     * Get SAAS Manager by email address
     *
     * @param email Email address to search for
     * @return SaasManager entity
     * @throws UnauthorizedException if manager not found
     */
    @Transactional(readOnly = true)
    public SaasManager getSaasManagerByEmail(String email) {
        return saasManagerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("SAAS Manager authentication failed - account not found - email: {}", email);
                    return new UnauthorizedException("Invalid email or password");
                });
    }
}
