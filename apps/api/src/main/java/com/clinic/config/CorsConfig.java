package com.clinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    private final SecurityProperties securityProperties;

    public CorsConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration publicCors = baseCorsConfiguration();
        applyOrigins(publicCors, securityProperties.cors().publicOrigins());

        CorsConfiguration adminCors = baseCorsConfiguration();
        applyOrigins(adminCors, securityProperties.cors().adminOrigins());

        // SAAS Manager CORS configuration
        CorsConfiguration saasCors = baseCorsConfiguration();
        applyOrigins(saasCors, securityProperties.cors().adminOrigins());

        // Fallback CORS for any other paths: merge public + admin origins
        CorsConfiguration mergedCors = baseCorsConfiguration();
        mergedCors.setAllowedOrigins(null); // reset before applying
        mergedCors.setAllowedOriginPatterns(null);
        applyOrigins(mergedCors, mergeOrigins(
                securityProperties.cors().publicOrigins(),
                securityProperties.cors().adminOrigins()
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/public/**", publicCors);
        source.registerCorsConfiguration("/api/public/**", publicCors);
        source.registerCorsConfiguration("/admin/**", adminCors);
        source.registerCorsConfiguration("/api/admin/**", adminCors);
        source.registerCorsConfiguration("/saas/**", saasCors);
        source.registerCorsConfiguration("/api/saas/**", saasCors);
        // Catch-all so endpoints like /auth/** or other paths are covered
        source.registerCorsConfiguration("/**", mergedCors);
        return source;
    }

    private CorsConfiguration baseCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        return configuration;
    }

    private void applyOrigins(CorsConfiguration configuration, List<String> origins) {
        // If "*" is provided, allow any origin (use patterns so browser echoes Origin when credentials are used)
        if (origins != null && origins.stream().anyMatch(o -> "*".equals(o))) {
            configuration.setAllowedOrigins(List.of());
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            // Add explicitly configured origins
            configuration.setAllowedOrigins(origins);
            configuration.setAllowedOriginPatterns(origins);
        }
        
        // Development: Allow localhost with any port
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("http://127.0.0.1:*");
        configuration.addAllowedOriginPattern("https://localhost:*");
        configuration.addAllowedOriginPattern("https://127.0.0.1:*");
        
        // Multi-tenant: Allow any subdomain on localhost (for development)
        // Matches: http://tenant-a.localhost:3001, http://clinica.localhost:3001, etc.
        configuration.addAllowedOriginPattern("http://*.localhost:*");
        configuration.addAllowedOriginPattern("https://*.localhost:*");
        
        // Multi-tenant: Allow any subdomain on configured base domain (for production)
        // This will match tenant subdomains like: https://tenant-a.yourdomain.com
        String baseDomain = securityProperties.cors().baseDomain();
        if (baseDomain != null && !baseDomain.isEmpty()) {
            configuration.addAllowedOriginPattern("http://*." + baseDomain);
            configuration.addAllowedOriginPattern("https://*." + baseDomain);
            // Also allow the base domain itself
            configuration.addAllowedOriginPattern("http://" + baseDomain);
            configuration.addAllowedOriginPattern("https://" + baseDomain);
        }
    }

    private List<String> mergeOrigins(List<String> a, List<String> b) {
        // Defensive merge to handle nulls
        List<String> merged = new java.util.ArrayList<>();
        if (a != null) merged.addAll(a);
        if (b != null) merged.addAll(b);
        if (merged.isEmpty()) {
            merged.add("*");
        }
        return merged;
    }
}
