package com.clinic.modules.admin.auth.service;

import com.clinic.config.SecurityProperties;
import com.clinic.modules.admin.auth.dto.AuthTokensResponse;
import com.clinic.modules.admin.auth.dto.LogoutRequest;
import com.clinic.modules.admin.auth.dto.RefreshTokenRequest;
import com.clinic.modules.admin.auth.dto.StaffLoginRequest;
import com.clinic.modules.admin.staff.model.StaffRefreshToken;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffRefreshTokenRepository;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.security.JwtIssuer;
import com.clinic.security.TotpService;
import com.clinic.modules.core.tenant.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class StaffAuthService {

    private final StaffUserRepository staffUserRepository;
    private final StaffRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;
    private final JwtIssuer jwtIssuer;
    private final SecurityProperties securityProperties;
    private final TenantContextHolder tenantContextHolder;

    public StaffAuthService(StaffUserRepository staffUserRepository,
                            StaffRefreshTokenRepository refreshTokenRepository,
                            PasswordEncoder passwordEncoder,
                            TotpService totpService,
                            JwtIssuer jwtIssuer,
                            SecurityProperties securityProperties,
                            TenantContextHolder tenantContextHolder) {
        this.staffUserRepository = staffUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.totpService = totpService;
        this.jwtIssuer = jwtIssuer;
        this.securityProperties = securityProperties;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Transactional
    public AuthTokensResponse login(StaffLoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        Long tenantId = tenantContextHolder.requireTenantId();
        StaffUser staffUser = staffUserRepository.findByEmailIgnoreCaseAndTenantId(normalizedEmail, tenantId)
                .orElseThrow(() -> unauthorized("Invalid credentials"));

        if (staffUser.getStatus() != StaffStatus.ACTIVE) {
            throw unauthorized("Account is not active");
        }

        if (!passwordEncoder.matches(request.password(), staffUser.getPasswordHash())) {
            throw unauthorized("Invalid credentials");
        }

        if (staffUser.isTwoFactorEnabled()) {
            if (!StringUtils.hasText(request.twoFactorCode()) ||
                    !totpService.verifyCode(staffUser.getTwoFactorSecret(), request.twoFactorCode())) {
                throw unauthorized("Invalid two-factor code");
            }
        }

        purgeExpiredTokens();

        var accessToken = jwtIssuer.issueStaffAccessToken(staffUser);
        var refreshToken = createRefreshToken(staffUser);

        return AuthTokensResponse.bearer(
                accessToken.token(),
                accessToken.expiresAt(),
                refreshToken.getToken(),
                refreshToken.getExpiresAt()
        );
    }

    @Transactional
    public AuthTokensResponse refresh(RefreshTokenRequest request) {
        purgeExpiredTokens();
        var storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> unauthorized("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw unauthorized("Refresh token expired or revoked");
        }

        var user = storedToken.getUser();
        var accessToken = jwtIssuer.issueStaffAccessToken(user);

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
        var replacement = createRefreshToken(user);

        return AuthTokensResponse.bearer(
                accessToken.token(),
                accessToken.expiresAt(),
                replacement.getToken(),
                replacement.getExpiresAt()
        );
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void changePassword(Long staffId, String currentPassword, String newPassword) {
        StaffUser staffUser = staffUserRepository.findByIdAndTenantId(staffId, tenantContextHolder.requireTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff user not found"));

        if (!passwordEncoder.matches(currentPassword, staffUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }

        if (passwordEncoder.matches(newPassword, staffUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password.");
        }

        staffUser.setPasswordHash(passwordEncoder.encode(newPassword));
        staffUserRepository.save(staffUser);

        refreshTokenRepository.findAllByUserAndRevokedIsFalse(staffUser).forEach(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    private StaffRefreshToken createRefreshToken(StaffUser user) {
        Instant expiresAt = Instant.now().plus(securityProperties.jwt().refresh().ttl());
        StaffRefreshToken refreshToken = new StaffRefreshToken(user, UUID.randomUUID().toString(), expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    private void purgeExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
