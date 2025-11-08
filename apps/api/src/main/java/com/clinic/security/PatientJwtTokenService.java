package com.clinic.security;

import com.clinic.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PatientJwtTokenService implements JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(PatientJwtTokenService.class);

    private final SecurityProperties securityProperties;
    private final JwtVerifier jwtVerifier;

    public PatientJwtTokenService(SecurityProperties securityProperties, JwtVerifier jwtVerifier) {
        this.securityProperties = securityProperties;
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Optional<JwtPrincipal> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            var config = securityProperties.jwt().patient();
            JwtPrincipal principal = jwtVerifier.verify(token, config);
            return Optional.of(principal);
        } catch (InvalidJwtException ex) {
            log.debug("Patient JWT rejected: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
