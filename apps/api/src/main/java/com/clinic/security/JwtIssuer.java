package com.clinic.security;

import com.clinic.config.SecurityProperties;
import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.core.patient.PatientEntity;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class JwtIssuer {

    private final SecurityProperties securityProperties;
    private final RSASSASigner staffSigner;
    private final RSASSASigner patientSigner;
    private final Environment environment;

    public JwtIssuer(SecurityProperties securityProperties, Environment environment) {
        this.securityProperties = securityProperties;
        this.environment = environment;
        this.staffSigner = createSigner(
                securityProperties.jwt().staff(),
                "JWT_STAFF_PRIVATE_KEY",
                "staff",
                "classpath:keys/staff_private.pem"
        );
        this.patientSigner = createSigner(
                securityProperties.jwt().patient(),
                "JWT_PATIENT_PRIVATE_KEY",
                "patient",
                "classpath:keys/patient_private.pem"
        );
    }

    public IssuedToken issueStaffAccessToken(StaffUser user) {
        var tokenConfig = securityProperties.jwt().staff();
        String issuer = resolve(tokenConfig.issuer(), "JWT_STAFF_ISSUER", "https://api.example-clinic.com");
        String audience = resolve(tokenConfig.audience(), "JWT_STAFF_AUDIENCE", "staff");
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(tokenConfig.accessTtl());

        var claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer(issuer)
                .audience(audience)
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(expiresAt))
                .claim("email", user.getEmail())
                .claim("name", user.getFullName())
                .claim("roles", List.of(roleToAuthority(user.getRole())))
                .build();

        return signToken(claims, staffSigner, "staff");
    }

    public IssuedToken issuePatientAccessToken(PatientEntity patient) {
        var tokenConfig = securityProperties.jwt().patient();
        String issuer = resolve(tokenConfig.issuer(), "JWT_PATIENT_ISSUER", "https://api.example-clinic.com");
        String audience = resolve(tokenConfig.audience(), "JWT_PATIENT_AUDIENCE", "patient");
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(tokenConfig.accessTtl());

        var claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(patient.getId()))
                .issuer(issuer)
                .audience(audience)
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(expiresAt))
                .claim("email", patient.getEmail())
                .claim("name", (patient.getFirstName() + " " + patient.getLastName()).trim())
                .claim("roles", List.of("ROLE_PATIENT"))
                .build();

        return signToken(claims, patientSigner, "patient");
    }

    private IssuedToken signToken(JWTClaimsSet claims, RSASSASigner signer, String label) {
        try {
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claims
            );
            signedJWT.sign(signer);
            Instant expiresAt = claims.getExpirationTime().toInstant();
            return new IssuedToken(signedJWT.serialize(), expiresAt);
        } catch (JOSEException ex) {
            throw new IllegalStateException("Unable to sign " + label + " JWT", ex);
        }
    }

    private RSASSASigner createSigner(SecurityProperties.Token tokenConfig,
                                      String envKey,
                                      String label,
                                      String defaultResource) {
        String pem = tokenConfig.privateKey();
        if (!StringUtils.hasText(pem)) {
            pem = environment.getProperty(envKey);
        }
        if (!StringUtils.hasText(pem)) {
            pem = defaultResource;
        }
        RSAKey key = parseRsaKey(pem, label);
        try {
            return new RSASSASigner(key);
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to initialize " + label + " JWT signer", e);
        }
    }

    private String roleToAuthority(StaffRole role) {
        return "ROLE_" + role.name();
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

    private RSAKey parseRsaKey(String pem, String label) {
        if (!StringUtils.hasText(pem)) {
            throw new IllegalStateException("Missing RSA private key for " + label + " tokens");
        }

        final String normalized;
        try {
            normalized = PemUtils.loadPem(pem)
                    .replace("\\n", "\n")
                    .trim();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unable to resolve RSA private key for " + label + " tokens", ex);
        }

        try {
            return (RSAKey) JWK.parseFromPEMEncodedObjects(normalized);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse RSA private key for " + label + " tokens", e);
        }
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }
}
