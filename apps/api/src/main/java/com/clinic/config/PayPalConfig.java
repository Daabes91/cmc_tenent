package com.clinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "paypal")
public class PayPalConfig {

    private String clientId;
    private String clientSecret;
    private String webhookId;
    private String environment = "sandbox";
    private String baseUrl = "https://api-m.sandbox.paypal.com";

    @Bean
    public RestTemplate paypalRestTemplate() {
        return new RestTemplate();
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
        // Update base URL based on environment
        if ("live".equals(environment) || "production".equals(environment)) {
            this.baseUrl = "https://api-m.paypal.com";
        } else {
            this.baseUrl = "https://api-m.sandbox.paypal.com";
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}