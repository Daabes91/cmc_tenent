package com.clinic.modules.admin.staff.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing granular permissions for a staff member.
 * Permissions are stored as JSON arrays for each module.
 */
@Entity
@Table(name = "staff_permissions")
public class StaffPermissions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_user_id", nullable = false, unique = true)
    private Long staffUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "appointments_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> appointmentsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calendar_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> calendarPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "patients_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> patientsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "doctors_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> doctorsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "materials_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> materialsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "services_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> servicesPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "insurance_companies_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> insuranceCompaniesPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "treatment_plans_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> treatmentPlansPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reports_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> reportsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> billingPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "translations_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> translationsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> settingsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "clinic_settings_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> clinicSettingsPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "staff_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> staffPermissions = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blogs_permissions", columnDefinition = "jsonb")
    private Set<PermissionAction> blogsPermissions = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public StaffPermissions() {
    }

    public StaffPermissions(Long staffUserId) {
        this.staffUserId = staffUserId;
    }

    // Helper method to get permissions for a specific module
    public Set<PermissionAction> getPermissionsForModule(ModuleName module) {
        return switch (module) {
            case APPOINTMENTS -> appointmentsPermissions;
            case CALENDAR -> calendarPermissions;
            case PATIENTS -> patientsPermissions;
            case DOCTORS -> doctorsPermissions;
            case MATERIALS -> materialsPermissions;
            case SERVICES -> servicesPermissions;
            case INSURANCE_COMPANIES -> insuranceCompaniesPermissions;
            case TREATMENT_PLANS -> treatmentPlansPermissions;
            case REPORTS -> reportsPermissions;
            case BILLING -> billingPermissions;
            case TRANSLATIONS -> translationsPermissions;
            case SETTINGS -> settingsPermissions;
            case CLINIC_SETTINGS -> clinicSettingsPermissions;
            case STAFF -> staffPermissions;
            case BLOGS -> blogsPermissions;
        };
    }

    // Helper method to set permissions for a specific module
    public void setPermissionsForModule(ModuleName module, Set<PermissionAction> permissions) {
        switch (module) {
            case APPOINTMENTS -> appointmentsPermissions = permissions;
            case CALENDAR -> calendarPermissions = permissions;
            case PATIENTS -> patientsPermissions = permissions;
            case DOCTORS -> doctorsPermissions = permissions;
            case MATERIALS -> materialsPermissions = permissions;
            case SERVICES -> servicesPermissions = permissions;
            case INSURANCE_COMPANIES -> insuranceCompaniesPermissions = permissions;
            case TREATMENT_PLANS -> treatmentPlansPermissions = permissions;
            case REPORTS -> reportsPermissions = permissions;
            case BILLING -> billingPermissions = permissions;
            case TRANSLATIONS -> translationsPermissions = permissions;
            case SETTINGS -> settingsPermissions = permissions;
            case CLINIC_SETTINGS -> clinicSettingsPermissions = permissions;
            case STAFF -> staffPermissions = permissions;
            case BLOGS -> blogsPermissions = permissions;
        }
    }

    // Helper method to check if a specific permission exists for a module
    public boolean hasPermission(ModuleName module, PermissionAction action) {
        Set<PermissionAction> permissions = getPermissionsForModule(module);
        return permissions != null && permissions.contains(action);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStaffUserId() {
        return staffUserId;
    }

    public void setStaffUserId(Long staffUserId) {
        this.staffUserId = staffUserId;
    }

    public Set<PermissionAction> getAppointmentsPermissions() {
        return appointmentsPermissions;
    }

    public void setAppointmentsPermissions(Set<PermissionAction> appointmentsPermissions) {
        this.appointmentsPermissions = appointmentsPermissions;
    }

    public Set<PermissionAction> getCalendarPermissions() {
        return calendarPermissions;
    }

    public void setCalendarPermissions(Set<PermissionAction> calendarPermissions) {
        this.calendarPermissions = calendarPermissions;
    }

    public Set<PermissionAction> getPatientsPermissions() {
        return patientsPermissions;
    }

    public void setPatientsPermissions(Set<PermissionAction> patientsPermissions) {
        this.patientsPermissions = patientsPermissions;
    }

    public Set<PermissionAction> getDoctorsPermissions() {
        return doctorsPermissions;
    }

    public void setDoctorsPermissions(Set<PermissionAction> doctorsPermissions) {
        this.doctorsPermissions = doctorsPermissions;
    }

    public Set<PermissionAction> getMaterialsPermissions() {
        return materialsPermissions;
    }

    public void setMaterialsPermissions(Set<PermissionAction> materialsPermissions) {
        this.materialsPermissions = materialsPermissions;
    }

    public Set<PermissionAction> getServicesPermissions() {
        return servicesPermissions;
    }

    public void setServicesPermissions(Set<PermissionAction> servicesPermissions) {
        this.servicesPermissions = servicesPermissions;
    }

    public Set<PermissionAction> getInsuranceCompaniesPermissions() {
        return insuranceCompaniesPermissions;
    }

    public void setInsuranceCompaniesPermissions(Set<PermissionAction> insuranceCompaniesPermissions) {
        this.insuranceCompaniesPermissions = insuranceCompaniesPermissions;
    }

    public Set<PermissionAction> getTreatmentPlansPermissions() {
        return treatmentPlansPermissions;
    }

    public void setTreatmentPlansPermissions(Set<PermissionAction> treatmentPlansPermissions) {
        this.treatmentPlansPermissions = treatmentPlansPermissions;
    }

    public Set<PermissionAction> getReportsPermissions() {
        return reportsPermissions;
    }

    public void setReportsPermissions(Set<PermissionAction> reportsPermissions) {
        this.reportsPermissions = reportsPermissions;
    }

    public Set<PermissionAction> getBillingPermissions() {
        return billingPermissions;
    }

    public void setBillingPermissions(Set<PermissionAction> billingPermissions) {
        this.billingPermissions = billingPermissions;
    }

    public Set<PermissionAction> getTranslationsPermissions() {
        return translationsPermissions;
    }

    public void setTranslationsPermissions(Set<PermissionAction> translationsPermissions) {
        this.translationsPermissions = translationsPermissions;
    }

    public Set<PermissionAction> getSettingsPermissions() {
        return settingsPermissions;
    }

    public void setSettingsPermissions(Set<PermissionAction> settingsPermissions) {
        this.settingsPermissions = settingsPermissions;
    }

    public Set<PermissionAction> getClinicSettingsPermissions() {
        return clinicSettingsPermissions;
    }

    public void setClinicSettingsPermissions(Set<PermissionAction> clinicSettingsPermissions) {
        this.clinicSettingsPermissions = clinicSettingsPermissions;
    }

    public Set<PermissionAction> getStaffPermissions() {
        return staffPermissions;
    }

    public void setStaffPermissions(Set<PermissionAction> staffPermissions) {
        this.staffPermissions = staffPermissions;
    }

    public Set<PermissionAction> getBlogsPermissions() {
        return blogsPermissions;
    }

    public void setBlogsPermissions(Set<PermissionAction> blogsPermissions) {
        this.blogsPermissions = blogsPermissions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
