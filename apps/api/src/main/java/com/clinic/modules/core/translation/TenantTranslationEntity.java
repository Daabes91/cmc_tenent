package com.clinic.modules.core.translation;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tenant_translations", uniqueConstraints = {
        @UniqueConstraint(
                name = "ux_tenant_translations_unique",
                columnNames = {"tenant_id", "namespace", "translation_key", "locale"}
        )
})
public class TenantTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "namespace", nullable = false, length = 100)
    private String namespace;

    @Column(name = "translation_key", nullable = false, length = 200)
    private String translationKey;

    @Column(name = "locale", nullable = false, length = 8)
    private String locale;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
