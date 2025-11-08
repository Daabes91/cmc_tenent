package com.clinic.security;

import com.clinic.config.SecurityProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class RsaJwtVerifier implements JwtVerifier {

    private static final Logger log = LoggerFactory.getLogger(RsaJwtVerifier.class);

    private final SecurityProperties securityProperties;
    private final Map<String, RSAKey> keyCache = new ConcurrentHashMap<>();

    public RsaJwtVerifier(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public JwtPrincipal verify(String token, SecurityProperties.Token tokenConfig) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String rawKey = tokenConfig.publicKey();
            boolean isStaffConfig = tokenConfig == securityProperties.jwt().staff();
            boolean isPatientConfig = tokenConfig == securityProperties.jwt().patient();
            boolean isSaasManagerConfig = tokenConfig == securityProperties.jwt().saasManager();
            if (log.isDebugEnabled()) {
                log.debug("Token config audience: '{}' (staff config: {})", tokenConfig.audience(), isStaffConfig);
            }
            if (!StringUtils.hasText(rawKey) && isStaffConfig) {
                log.debug("Staff public key missing from configuration; falling back to bundled development key");
                rawKey = "classpath:keys/staff_public.pem";
            } else if (!StringUtils.hasText(rawKey) && isPatientConfig) {
                log.debug("Patient public key missing from configuration; falling back to bundled development key");
                rawKey = "classpath:keys/patient_public.pem";
            } else if (!StringUtils.hasText(rawKey) && isSaasManagerConfig) {
                log.debug("SAAS Manager public key missing from configuration; falling back to staff key");
                rawKey = "classpath:keys/staff_public.pem";
            }
            RSAKey rsaKey = resolveKey(rawKey);
            RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);

            if (!signedJWT.verify(verifier)) {
                throw new InvalidJwtException("JWT signature verification failed");
            }

            var claims = signedJWT.getJWTClaimsSet();
            validateClaims(claims, tokenConfig);

            String subject = claims.getSubject();
            String audience = claims.getAudience().isEmpty() ? null : claims.getAudience().get(0);
            String issuer = claims.getIssuer();
            String expectedAudience = StringUtils.hasText(tokenConfig.audience())
                    ? tokenConfig.audience()
                    : (isStaffConfig ? "staff" : (isSaasManagerConfig ? "saas-manager" : "patient"));
            String expectedIssuer = StringUtils.hasText(tokenConfig.issuer())
                    ? tokenConfig.issuer()
                    : "https://api.example-clinic.com";

            if (subject == null || subject.isBlank()) {
                throw new InvalidJwtException("JWT subject missing");
            }

            if (!Objects.equals(audience, expectedAudience)) {
                throw new InvalidJwtException("JWT audience mismatch");
            }

            if (!Objects.equals(issuer, expectedIssuer)) {
                throw new InvalidJwtException("JWT issuer mismatch");
            }

            List<String> roles = extractRoles(claims.getClaim("roles"));
            Long tenantId = extractTenantId(claims);

            JwtAudience audienceEnum;
            if (tokenConfig.audience().equalsIgnoreCase("staff")) {
                audienceEnum = JwtAudience.STAFF;
            } else if (tokenConfig.audience().equalsIgnoreCase("saas-manager")) {
                audienceEnum = JwtAudience.SAAS_MANAGER;
            } else {
                audienceEnum = JwtAudience.PATIENT;
            }

            return new JwtPrincipal(subject, audienceEnum, roles, tenantId);
        } catch (ParseException | JOSEException e) {
            throw new InvalidJwtException("Failed to parse JWT", e);
        }
    }

    private RSAKey resolveKey(String rawKey) {
        return keyCache.computeIfAbsent(rawKey, key -> {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Resolving RSA key from config value: {}", preview(key));
                }
                String normalised = PemUtils.loadPem(key).replace("\\n", "\n");
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to parse RSA key: {}", preview(normalised));
                }
                return (RSAKey) JWK.parseFromPEMEncodedObjects(normalised);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse RSA public key", e);
            }
        });
    }

    private String preview(String key) {
        String singleLine = key.replace("\n", "\\n");
        return singleLine.length() > 80 ? singleLine.substring(0, 80) + "..." : singleLine;
    }

    private void validateClaims(com.nimbusds.jwt.JWTClaimsSet claims, SecurityProperties.Token tokenConfig) {
        Instant now = Instant.now();
        Duration allowedSkew = tokenConfig.clockSkew() != null ? tokenConfig.clockSkew() : Duration.ofSeconds(30);

        Instant expiration = claims.getExpirationTime() != null ? claims.getExpirationTime().toInstant() : null;
        Instant notBefore = claims.getNotBeforeTime() != null ? claims.getNotBeforeTime().toInstant() : null;

        if (expiration == null) {
            throw new InvalidJwtException("JWT expiration required");
        }

        if (expiration.plus(allowedSkew).isBefore(now)) {
            throw new InvalidJwtException("JWT expired");
        }

        if (notBefore != null && notBefore.minus(allowedSkew).isAfter(now)) {
            throw new InvalidJwtException("JWT not yet valid");
        }
    }

    private List<String> extractRoles(Object rolesClaim) {
        if (rolesClaim instanceof List<?> roleList) {
            return roleList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(this::normalizeRole)
                    .collect(Collectors.toList());
        }

        if (rolesClaim instanceof String roleString) {
            return List.of(normalizeRole(roleString));
        }

        log.debug("JWT roles claim missing or invalid, defaulting to ROLE_PATIENT");
        return List.of("ROLE_PATIENT");
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_PATIENT";
        }
        return role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase(Locale.ROOT);
    }

    private Long extractTenantId(com.nimbusds.jwt.JWTClaimsSet claims) {
        try {
            Object tenantIdClaim = claims.getClaim("tenantId");
            if (tenantIdClaim == null) {
                return null;
            }
            if (tenantIdClaim instanceof Number) {
                return ((Number) tenantIdClaim).longValue();
            }
            if (tenantIdClaim instanceof String) {
                return Long.parseLong((String) tenantIdClaim);
            }
            return null;
        } catch (Exception e) {
            log.debug("Failed to extract tenantId from JWT claims", e);
            return null;
        }
    }
}
