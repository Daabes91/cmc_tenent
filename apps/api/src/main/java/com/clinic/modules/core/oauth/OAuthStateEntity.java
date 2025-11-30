package com.clinic.modules.core.oauth;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity for storing temporary OAuth state parameters during the Google OAuth flow.
 * States are used to maintain tenant context and prevent CSRF attacks.
 * They expire after 5 minutes and are consumed after a single use.
 */
@Entity
@Table(name = "oauth_states")
public class OAuthStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "state_token", nullable = false, unique = true, length = 255)
    private String stateToken;

    @Column(name = "tenant_slug", nullable = false, length = 100)
    private String tenantSlug;

    @Column(nullable = false, length = 255)
    private String nonce;

    @Column(name = "redirect_uri", columnDefinition = "TEXT")
    private String redirectUri;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean consumed = false;

    protected OAuthStateEntity() {
    }

    public OAuthStateEntity(String stateToken, String tenantSlug, String nonce, String redirectUri, Instant expiresAt) {
        this.stateToken = stateToken;
        this.tenantSlug = tenantSlug;
        this.nonce = nonce;
        this.redirectUri = redirectUri;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void onCreate() {
        // Only set createdAt if it hasn't been set already (for testing purposes)
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getStateToken() {
        return stateToken;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public String getNonce() {
        return nonce;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void markConsumed() {
        this.consumed = true;
    }

    /**
     * Check if this state has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if this state is valid (not expired and not consumed)
     */
    public boolean isValid() {
        return !isExpired() && !consumed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuthStateEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
