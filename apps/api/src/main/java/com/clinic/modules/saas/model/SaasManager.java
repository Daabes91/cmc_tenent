package com.clinic.modules.saas.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Entity representing a SAAS Manager - a platform-level administrator
 * who manages multiple tenants across the entire system.
 * SAAS Managers operate independently from tenant-scoped staff users.
 */
@Entity
@Table(name = "saas_managers")
public class SaasManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SaasManagerStatus status = SaasManagerStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Default constructor for JPA
     */
    protected SaasManager() {
    }

    /**
     * Constructor for creating a new SAAS Manager
     *
     * @param email        Manager's email address (used for login)
     * @param fullName     Manager's full name
     * @param passwordHash BCrypt hashed password
     */
    public SaasManager(String email, String fullName, String passwordHash) {
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
    }

    /**
     * Lifecycle callback to set timestamps on entity creation
     */
    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Lifecycle callback to update timestamp on entity modification
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public SaasManagerStatus getStatus() {
        return status;
    }

    public void setStatus(SaasManagerStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Check if the manager account is active
     *
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return this.status == SaasManagerStatus.ACTIVE;
    }
}
