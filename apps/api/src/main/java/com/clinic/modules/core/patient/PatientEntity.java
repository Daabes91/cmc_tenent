package com.clinic.modules.core.patient;

import com.clinic.modules.core.tag.TagEntity;
import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "global_patient_id", nullable = false)
    private GlobalPatientEntity globalPatient;

    @Column(name = "external_id", nullable = false, unique = true, length = 64)
    private String externalId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(length = 160)
    private String email;

    @Column(length = 32)
    private String phone;

    /**
     * URL to the patient's profile image (stored in Cloudflare Images).
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /**
     * Google ID for this patient (tenant-scoped).
     * The same Google account can have different patient records across tenants.
     */
    @Column(name = "google_id", length = 255)
    private String googleId;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "drive_folder_url", length = 500)
    private String driveFolderUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "patient_tags", joinColumns = @JoinColumn(name = "patient_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<TagEntity> tags = new HashSet<>();

    protected PatientEntity() {
    }

    public PatientEntity(String firstName, String lastName, String email, String phone) {
        this.externalId = "PAT-" + UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
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

    public GlobalPatientEntity getGlobalPatient() {
        return globalPatient;
    }

    public void setGlobalPatient(GlobalPatientEntity globalPatient) {
        this.globalPatient = globalPatient;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDriveFolderUrl() {
        return driveFolderUrl;
    }

    public void setDriveFolderUrl(String driveFolderUrl) {
        this.driveFolderUrl = driveFolderUrl;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        // Enforce immutability: once set, Google ID cannot be changed
        if (this.googleId != null && !this.googleId.equals(googleId)) {
            throw new IllegalStateException("Google ID is immutable and cannot be changed once set");
        }
        this.googleId = googleId;
    }

    /**
     * Link a Google account to this patient record
     */
    public void linkGoogleAccount(String googleId) {
        if (this.googleId != null) {
            throw new IllegalStateException("Google account already linked to this patient");
        }
        this.googleId = googleId;
    }

    /**
     * Check if this patient has Google authentication linked
     */
    public boolean hasGoogleAuth() {
        return googleId != null && !googleId.isEmpty();
    }

    public void markLogin() {
        this.lastLoginAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<TagEntity> getTags() {
        return tags;
    }

    public void setTags(Set<TagEntity> tags) {
        this.tags = tags;
    }

    public void updateDetails(String firstName, String lastName, String email, String phone, LocalDate dateOfBirth,
            String notes, String driveFolderUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.notes = notes;
        this.driveFolderUrl = driveFolderUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PatientEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
