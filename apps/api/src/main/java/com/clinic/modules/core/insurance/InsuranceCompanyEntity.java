package com.clinic.modules.core.insurance;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "insurance_companies")
public class InsuranceCompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Column(name = "name_ar", length = 160)
    private String nameAr;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InsuranceCompanyEntity() {
    }

    public InsuranceCompanyEntity(String nameEn, String nameAr, String logoUrl, String websiteUrl, String phone, String email, String descriptionEn, String descriptionAr, Boolean isActive, Integer displayOrder) {
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.phone = phone;
        this.email = email;
        this.descriptionEn = descriptionEn;
        this.descriptionAr = descriptionAr;
        this.isActive = isActive != null ? isActive : true;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameAr() {
        return nameAr;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public String getDescriptionAr() {
        return descriptionAr;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    // Update method
    public void updateDetails(String nameEn, String nameAr, String logoUrl, String websiteUrl, String phone, String email, String descriptionEn, String descriptionAr, Boolean isActive, Integer displayOrder) {
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.phone = phone;
        this.email = email;
        this.descriptionEn = descriptionEn;
        this.descriptionAr = descriptionAr;
        this.isActive = isActive != null ? isActive : true;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InsuranceCompanyEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}