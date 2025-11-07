package com.clinic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.time.Duration;

/**
 * Manually creates SecurityProperties from environment variables that don't follow
 * Spring Boot's standard naming convention (missing SECURITY_ prefix).
 *
 * This is necessary because DigitalOcean environment variables are named JWT_STAFF_*
 * but @ConfigurationProperties(prefix="security") expects SECURITY_JWT_STAFF_*.
 */
@Configuration
@Profile("prod")
public class JwtEnvironmentPropertiesConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtEnvironmentPropertiesConfig.class);

    @Bean
    @Primary
    public SecurityProperties securityPropertiesWithEnvOverrides(Environment environment) {
        log.info("Creating SecurityProperties with environment variable overrides");

        SecurityProperties properties = new SecurityProperties();

        // Staff JWT configuration from environment
        String staffIssuer = environment.getProperty("JWT_STAFF_ISSUER");
        String staffAudience = environment.getProperty("JWT_STAFF_AUDIENCE");
        String staffPublicKey = environment.getProperty("JWT_STAFF_PUBLIC_KEY");
        String staffPrivateKey = environment.getProperty("JWT_STAFF_PRIVATE_KEY");

        // Patient JWT configuration from environment
        String patientIssuer = environment.getProperty("JWT_PATIENT_ISSUER");
        String patientAudience = environment.getProperty("JWT_PATIENT_AUDIENCE");
        String patientPublicKey = environment.getProperty("JWT_PATIENT_PUBLIC_KEY");
        String patientPrivateKey = environment.getProperty("JWT_PATIENT_PRIVATE_KEY");

        if (staffIssuer != null || staffAudience != null || staffPublicKey != null) {
            log.info("Applying JWT configuration from environment");

            SecurityProperties.Token staffToken = new SecurityProperties.Token(
                staffIssuer != null ? staffIssuer : "",
                staffAudience != null ? staffAudience : "",
                staffPublicKey != null ? staffPublicKey : "",
                staffPrivateKey != null ? staffPrivateKey : "",
                Duration.ofDays(30),
                Duration.ofSeconds(30)
            );

            SecurityProperties.Token patientToken = new SecurityProperties.Token(
                patientIssuer != null ? patientIssuer : "",
                patientAudience != null ? patientAudience : "",
                patientPublicKey != null ? patientPublicKey : "",
                patientPrivateKey != null ? patientPrivateKey : "",
                Duration.ofDays(30),
                Duration.ofSeconds(30)
            );

            SecurityProperties.Refresh refresh = new SecurityProperties.Refresh(Duration.ofMinutes(30));
            SecurityProperties.Jwt jwt = new SecurityProperties.Jwt(patientToken, staffToken, refresh);
            properties.setJwt(jwt);

            log.info("JWT configuration applied successfully");
            log.info("Staff issuer: '{}'", staffIssuer);
            log.info("Staff audience: '{}'", staffAudience);
            log.info("Staff public key present: {}", staffPublicKey != null && !staffPublicKey.isBlank());
            log.info("Staff public key length: {}", staffPublicKey != null ? staffPublicKey.length() : 0);
        } else {
            log.warn("No JWT environment variables found - using defaults");
        }

        return properties;
    }
}
