package com.clinic.modules.saas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing PayPal API configuration for subscription billing.
 * Managed by SaaS administrators through the admin panel.
 */
@Entity
@Table(name = "paypal_config")
public class PayPalConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 255)
    private String clientId;

    @Column(name = "client_secret_encrypted", nullable = false, columnDefinition = "TEXT")
    private String clientSecretEncrypted;

    @Column(name = "plan_id", length = 255)
    private String planId;

    @Column(name = "plan_config", columnDefinition = "TEXT")
    private String planConfig;

    @Column(name = "webhook_id", length = 255)
    private String webhookId;

    @Column(name = "sandbox_mode", nullable = false)
    private Boolean sandboxMode = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecretEncrypted() {
        return clientSecretEncrypted;
    }

    public void setClientSecretEncrypted(String clientSecretEncrypted) {
        this.clientSecretEncrypted = clientSecretEncrypted;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public String getPlanConfig() {
        return planConfig;
    }

    public void setPlanConfig(String planConfig) {
        this.planConfig = planConfig;
    }

    public Boolean getSandboxMode() {
        return sandboxMode;
    }

    public void setSandboxMode(Boolean sandboxMode) {
        this.sandboxMode = sandboxMode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
