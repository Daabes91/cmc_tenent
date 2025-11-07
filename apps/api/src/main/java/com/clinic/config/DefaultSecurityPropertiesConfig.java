package com.clinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Provides SecurityProperties bean for non-production profiles.
 * Uses standard Spring Boot @ConfigurationProperties binding.
 */
@Configuration
@Profile("!prod")
public class DefaultSecurityPropertiesConfig {

    @Bean
    @ConfigurationProperties(prefix = "security")
    public SecurityProperties securityProperties() {
        return new SecurityProperties();
    }
}
