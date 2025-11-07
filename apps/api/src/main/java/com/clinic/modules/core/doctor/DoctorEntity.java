package com.clinic.modules.core.doctor;

import com.clinic.modules.core.service.ClinicServiceEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "doctors")
public class DoctorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name_en", nullable = false, length = 160)
    private String fullNameEn;

    @Column(name = "full_name_ar", length = 160)
    private String fullNameAr;

    @Column(name = "specialty_en", length = 120)
    private String specialtyEn;

    @Column(name = "specialty_ar", length = 120)
    private String specialtyAr;

    @Column(name = "bio_en", columnDefinition = "TEXT")
    private String bioEn;

    @Column(name = "bio_ar", columnDefinition = "TEXT")
    private String bioAr;

    /**
     * URL to the doctor's profile image (stored in Cloudflare Images).
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Comma separated ISO language codes (e.g. "en,ar").
     */
    @Column(name = "locale", length = 32)
    private String localeCodes;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany
    @JoinTable(
            name = "doctor_services",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<ClinicServiceEntity> services = new HashSet<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DoctorAvailabilityEntity> availabilities = new HashSet<>();

    protected DoctorEntity() {
    }

    public DoctorEntity(String fullNameEn, String fullNameAr, String specialtyEn, String specialtyAr, String bioEn, String bioAr, String localeCodes) {
        this.fullNameEn = fullNameEn;
        this.fullNameAr = fullNameAr;
        this.specialtyEn = specialtyEn;
        this.specialtyAr = specialtyAr;
        this.bioEn = bioEn;
        this.bioAr = bioAr;
        this.localeCodes = localeCodes;
        this.displayOrder = 0;
        this.isActive = true;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getFullNameEn() {
        return fullNameEn;
    }

    public String getFullNameAr() {
        return fullNameAr;
    }

    public String getSpecialtyEn() {
        return specialtyEn;
    }

    public String getSpecialtyAr() {
        return specialtyAr;
    }

    public String getBioEn() {
        return bioEn;
    }

    public String getBioAr() {
        return bioAr;
    }

    // Convenience methods for backward compatibility
    public String getFullName() {
        return fullNameEn != null ? fullNameEn : fullNameAr;
    }

    public String getSpecialty() {
        return specialtyEn != null ? specialtyEn : specialtyAr;
    }

    public String getBio() {
        return bioEn != null ? bioEn : bioAr;
    }

    public String getLocaleCodes() {
        return localeCodes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<ClinicServiceEntity> getServices() {
        return services;
    }

    public Set<DoctorAvailabilityEntity> getAvailabilities() {
        return availabilities;
    }

    public void updateDetails(String fullNameEn, String fullNameAr, String specialtyEn, String specialtyAr, String bioEn, String bioAr, String localeCodes) {
        this.fullNameEn = fullNameEn;
        this.fullNameAr = fullNameAr;
        this.specialtyEn = specialtyEn;
        this.specialtyAr = specialtyAr;
        this.bioEn = bioEn;
        this.bioAr = bioAr;
        this.localeCodes = localeCodes;
    }

    public void updateContactInfo(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public void updateDisplaySettings(Integer displayOrder, Boolean isActive) {
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isActive = isActive != null ? isActive : true;
    }

    // Convenience method for backward compatibility
    public void updateDetails(String fullName, String specialty, String bio, String localeCodes) {
        this.fullNameEn = fullName;
        this.specialtyEn = specialty;
        this.bioEn = bio;
        this.localeCodes = localeCodes;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void assignServices(Set<ClinicServiceEntity> services) {
        this.services.clear();
        if (services != null) {
            this.services.addAll(services);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorEntity doctor)) return false;
        return Objects.equals(id, doctor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
