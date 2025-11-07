package com.clinic.security;

import com.clinic.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StaffJwtTokenService implements JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(StaffJwtTokenService.class);

    private final SecurityProperties securityProperties;
    private final JwtVerifier jwtVerifier;

    public StaffJwtTokenService(
            SecurityProperties securityProperties,
            JwtVerifier jwtVerifier
    ) {
        this.securityProperties = securityProperties;
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Optional<JwtPrincipal> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            var config = securityProperties.jwt().staff();
            JwtPrincipal principal = jwtVerifier.verify(token, config);
            return Optional.of(new JwtPrincipal(principal.subject(), JwtAudience.STAFF, principal.roles()));
        } catch (InvalidJwtException ex) {
            log.debug("Staff JWT rejected: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
