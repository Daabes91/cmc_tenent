package com.clinic.modules.core.service;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.clinic.modules.core.doctor.DoctorEntity;

@Entity
@Table(name = "services")
public class ClinicServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "name_en", nullable = false, length = 160)
    private String nameEn;

    @Column(name = "name_ar", length = 160)
    private String nameAr;

    @Column(name = "summary_en")
    private String summaryEn;

    @Column(name = "summary_ar")
    private String summaryAr;

    /**
     * URL to the service's image (stored in Cloudflare Images).
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "services")
    private Set<DoctorEntity> doctors = new HashSet<>();

    protected ClinicServiceEntity() {
    }

    public ClinicServiceEntity(String slug, String nameEn, String nameAr, String summaryEn, String summaryAr) {
        this.slug = slug;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.summaryEn = summaryEn;
        this.summaryAr = summaryAr;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public void updateDetails(String slug, String nameEn, String nameAr, String summaryEn, String summaryAr) {
        this.slug = slug;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.summaryEn = summaryEn;
        this.summaryAr = summaryAr;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameAr() {
        return nameAr;
    }

    public String getSummaryEn() {
        return summaryEn;
    }

    public String getSummaryAr() {
        return summaryAr;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<DoctorEntity> getDoctors() {
        return doctors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClinicServiceEntity service)) return false;
        return Objects.equals(id, service.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
