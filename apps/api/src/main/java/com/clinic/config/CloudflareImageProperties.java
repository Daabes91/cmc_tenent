package com.clinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for Cloudflare Images API.
 */
@Component
@ConfigurationProperties(prefix = "security.cloudflare.images")
public class CloudflareImageProperties {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CloudflareImageProperties.class);

    private String accountId;
    private String apiToken;
    private String baseUrl;
    private String deliveryUrl;
    private int maxFileSizeMb;
    private String allowedExtensions;
    private int imageQuality = 85;
    private boolean autoOptimize = true;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
        log.info("CloudflareImageProperties: accountId set to: {}", accountId);
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDeliveryUrl() {
        return deliveryUrl;
    }

    public void setDeliveryUrl(String deliveryUrl) {
        this.deliveryUrl = deliveryUrl;
    }

    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public void setMaxFileSizeMb(int maxFileSizeMb) {
        this.maxFileSizeMb = maxFileSizeMb;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getAllowedExtensionsList() {
        return Arrays.asList(allowedExtensions.split(","));
    }

    public long getMaxFileSizeBytes() {
        return (long) maxFileSizeMb * 1024 * 1024;
    }

    public int getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

    public boolean isAutoOptimize() {
        return autoOptimize;
    }

    public void setAutoOptimize(boolean autoOptimize) {
        this.autoOptimize = autoOptimize;
    }
}
