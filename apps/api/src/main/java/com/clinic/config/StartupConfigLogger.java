package com.clinic.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs configuration values at startup to help diagnose environment variable injection issues.
 */
@Component
public class StartupConfigLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupConfigLogger.class);

    private final SecurityProperties securityProperties;
    private final Environment environment;

    public StartupConfigLogger(SecurityProperties securityProperties, Environment environment) {
        this.securityProperties = securityProperties;
        this.environment = environment;
    }

    @PostConstruct
    public void logStartupConfig() {
        log.info("=== Startup Configuration ===");
        log.info("Active profiles: {}", String.join(", ", environment.getActiveProfiles()));
        log.info("Default profiles: {}", String.join(", ", environment.getDefaultProfiles()));

        log.info("=== JWT Configuration (Staff) ===");
        var staffConfig = securityProperties.jwt().staff();
        log.info("Issuer: '{}'", staffConfig.issuer());
        log.info("Audience: '{}'", staffConfig.audience());
        log.info("Public key present: {}", staffConfig.publicKey() != null && !staffConfig.publicKey().isBlank());
        log.info("Public key length: {}", staffConfig.publicKey() != null ? staffConfig.publicKey().length() : 0);
        log.info("Public key preview: {}", preview(staffConfig.publicKey()));
        log.info("Private key present: {}", staffConfig.privateKey() != null && !staffConfig.privateKey().isBlank());

        log.info("=== JWT Configuration (Patient) ===");
        var patientConfig = securityProperties.jwt().patient();
        log.info("Issuer: '{}'", patientConfig.issuer());
        log.info("Audience: '{}'", patientConfig.audience());
        log.info("Public key present: {}", patientConfig.publicKey() != null && !patientConfig.publicKey().isBlank());
        log.info("Public key length: {}", patientConfig.publicKey() != null ? patientConfig.publicKey().length() : 0);
        log.info("Public key preview: {}", preview(patientConfig.publicKey()));

        log.info("=== Environment Variables (Raw) ===");
        log.info("JWT_STAFF_ISSUER from env: '{}'", environment.getProperty("JWT_STAFF_ISSUER", "(not set)"));
        log.info("JWT_STAFF_AUDIENCE from env: '{}'", environment.getProperty("JWT_STAFF_AUDIENCE", "(not set)"));
        log.info("JWT_STAFF_PUBLIC_KEY from env: {}",
            environment.getProperty("JWT_STAFF_PUBLIC_KEY") != null ?
            "set (length: " + environment.getProperty("JWT_STAFF_PUBLIC_KEY").length() + ")" :
            "(not set)");
        log.info("=============================");
    }

    private String preview(String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        String normalized = value.replace("\n", "\\n");
        return normalized.length() > 60 ? normalized.substring(0, 60) + "..." : normalized;
    }
}
