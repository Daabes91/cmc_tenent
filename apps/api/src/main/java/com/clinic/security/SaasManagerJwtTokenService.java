package com.clinic.security;

import com.clinic.config.SecurityProperties;
import com.clinic.modules.saas.model.SaasManager;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class SaasManagerJwtTokenService implements JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(SaasManagerJwtTokenService.class);

    private final SecurityProperties securityProperties;
    private final JwtVerifier jwtVerifier;
    private final RSASSASigner signer;
    private final Environment environment;

    public SaasManagerJwtTokenService(
            SecurityProperties securityProperties,
            JwtVerifier jwtVerifier,
            Environment environment
    ) {
        this.securityProperties = securityProperties;
        this.jwtVerifier = jwtVerifier;
        this.environment = environment;
        this.signer = createSigner();
    }

    /**
     * Generate JWT token for SAAS Manager with SAAS_MANAGER role claim
     */
    public IssuedToken generateToken(SaasManager saasManager) {
        var tokenConfig = securityProperties.jwt().saasManager();
        String issuer = resolve(tokenConfig.issuer(), "JWT_SAAS_MANAGER_ISSUER", "https://api.example-clinic.com");
        String audience = resolve(tokenConfig.audience(), "JWT_SAAS_MANAGER_AUDIENCE", "saas-manager");
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(tokenConfig.accessTtl());

        var claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(saasManager.getId()))
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .claim("email", saasManager.getEmail())
                .claim("name", saasManager.getFullName())
                .claim("roles", List.of("SAAS_MANAGER"))
                .build();

        return signToken(claims, expiresAt);
    }

    /**
     * Validate JWT token and return principal if valid
     */
    @Override
    public Optional<JwtPrincipal> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            var config = securityProperties.jwt().saasManager();
            JwtPrincipal principal = jwtVerifier.verify(token, config);
            return Optional.of(principal);
        } catch (InvalidJwtException ex) {
            log.debug("SAAS Manager JWT rejected: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private IssuedToken signToken(JWTClaimsSet claims, Instant expiresAt) {
        try {
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claims
            );
            signedJWT.sign(signer);
            return new IssuedToken(signedJWT.serialize(), expiresAt);
        } catch (JOSEException ex) {
            throw new IllegalStateException("Unable to sign SAAS Manager JWT", ex);
        }
    }

    private RSASSASigner createSigner() {
        var tokenConfig = securityProperties.jwt().saasManager();
        String pem = tokenConfig.privateKey();
        if (!StringUtils.hasText(pem)) {
            pem = environment.getProperty("JWT_SAAS_MANAGER_PRIVATE_KEY");
        }
        if (!StringUtils.hasText(pem)) {
            // Use staff keys as fallback for development
            pem = "classpath:keys/staff_private.pem";
            log.warn("SAAS Manager private key not configured, using staff key as fallback");
        }
        RSAKey key = parseRsaKey(pem);
        try {
            return new RSASSASigner(key);
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to initialize SAAS Manager JWT signer", e);
        }
    }

    private RSAKey parseRsaKey(String pem) {
        if (!StringUtils.hasText(pem)) {
            throw new IllegalStateException("Missing RSA private key for SAAS Manager tokens");
        }

        final String normalized;
        try {
            normalized = PemUtils.loadPem(pem)
                    .replace("\\n", "\n")
                    .trim();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unable to resolve RSA private key for SAAS Manager tokens", ex);
        }

        try {
            return (RSAKey) JWK.parseFromPEMEncodedObjects(normalized);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse RSA private key for SAAS Manager tokens", e);
        }
    }

    private String resolve(String currentValue, String envKey, String defaultValue) {
        if (currentValue != null && !currentValue.isBlank()) {
            return currentValue;
        }
        String envValue = environment.getProperty(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return defaultValue;
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }
}
