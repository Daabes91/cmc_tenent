package com.clinic.modules.core.patient;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Global patient entity representing a patient's identity across all tenants.
 * This entity stores core identity information (email, phone, password) that is shared
 * across all clinics. Each tenant has a separate PatientEntity profile linked to this global record.
 */
@Entity
@Table(name = "global_patients")
public class GlobalPatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 64)
    private String externalId;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "google_email", length = 255)
    private String googleEmail;

    @Column(name = "auth_provider", length = 20)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GlobalPatientEntity() {
    }

    /**
     * Constructor for local authentication (email/password)
     */
    public GlobalPatientEntity(String email, String phone, String passwordHash, LocalDate dateOfBirth) {
        this.externalId = "PAT-" + UUID.randomUUID();
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.dateOfBirth = dateOfBirth;
        this.authProvider = AuthProvider.LOCAL;
    }

    /**
     * Constructor for Google OAuth authentication (no password)
     */
    public GlobalPatientEntity(String googleId, String email, String firstName, String lastName) {
        this.externalId = "PAT-" + UUID.randomUUID();
        this.googleId = googleId;
        this.email = email;
        this.googleEmail = email;
        this.authProvider = AuthProvider.GOOGLE;
        // password_hash remains null for Google-only accounts
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public String getGoogleEmail() {
        return googleEmail;
    }

    public void setGoogleEmail(String googleEmail) {
        this.googleEmail = googleEmail;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    /**
     * Link a Google account to this existing local account
     */
    public void linkGoogleAccount(String googleId, String googleEmail) {
        if (this.googleId != null) {
            throw new IllegalStateException("Google account already linked");
        }
        this.googleId = googleId;
        this.googleEmail = googleEmail;
        this.authProvider = AuthProvider.BOTH;
    }

    /**
     * Check if this patient has Google authentication
     */
    public boolean hasGoogleAuth() {
        return googleId != null && !googleId.isEmpty();
    }

    /**
     * Check if this patient has local authentication
     */
    public boolean hasLocalAuth() {
        return passwordHash != null && !passwordHash.isEmpty();
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobalPatientEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
