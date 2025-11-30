package com.clinic.modules.core.oauth;

import com.clinic.modules.core.patient.*;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing patient authentication via Google OAuth.
 * Handles patient lookup, creation, and account linking with proper tenant scoping.
 * 
 * This service ensures:
 * - Multi-tenant isolation: Google IDs are scoped per tenant
 * - Account linking: Existing local accounts can be linked to Google
 * - New patient creation: New patients are created from Google user info
 * - Duplicate detection: Prevents duplicate accounts within a tenant
 */
@Service
public class PatientGoogleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(PatientGoogleAuthService.class);

    private final GlobalPatientRepository globalPatientRepository;
    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;
    private final OAuthMetricsService metricsService;

    public PatientGoogleAuthService(
            GlobalPatientRepository globalPatientRepository,
            PatientRepository patientRepository,
            TenantRepository tenantRepository,
            OAuthMetricsService metricsService
    ) {
        this.globalPatientRepository = globalPatientRepository;
        this.patientRepository = patientRepository;
        this.tenantRepository = tenantRepository;
        this.metricsService = metricsService;
    }

    /**
     * Authenticate or create patient via Google OAuth.
     * 
     * This method implements the following logic:
     * 1. Check if patient exists with this Google ID in this tenant -> return existing
     * 2. Check if global patient exists with this Google ID -> create tenant-scoped patient
     * 3. Check if patient exists with this email in this tenant -> link accounts
     * 4. Create new global patient and tenant-scoped patient
     * 
     * @param googleId Google user ID
     * @param email Email from Google
     * @param firstName First name from Google
     * @param lastName Last name from Google
     * @param tenantId Tenant ID for scoping
     * @return PatientAuthResult with patient information and flags
     */
    @Transactional
    public PatientAuthResult authenticateWithGoogle(
            String googleId,
            String email,
            String firstName,
            String lastName,
            Long tenantId
    ) {
        logger.info("Authenticating patient with Google: googleId={}, email={}, tenantId={}", 
                    googleId, email, tenantId);

        // Validate inputs
        if (googleId == null || googleId.isBlank()) {
            throw new IllegalArgumentException("Google ID cannot be null or empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }

        // Verify tenant exists
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    metricsService.recordTenantError(null, "tenant_not_found");
                    return new IllegalArgumentException("Tenant not found: " + tenantId);
                });

        // 1. Check if patient already exists with this Google ID in this tenant
        Optional<PatientEntity> existingPatient = findByGoogleIdAndTenant(googleId, tenantId);
        if (existingPatient.isPresent()) {
            PatientEntity patient = existingPatient.get();
            patient.markLogin();
            patientRepository.save(patient);
            
            logger.info("Found existing patient with Google ID in tenant: patientId={}", patient.getId());
            
            return new PatientAuthResult(
                patient.getId(),
                patient.getGlobalPatient().getId(),
                email,
                patient.getFirstName(),
                patient.getLastName(),
                false,  // not a new patient
                false   // not newly linked
            );
        }

        // 2. Check if global patient exists with this Google ID
        Optional<GlobalPatientEntity> existingGlobalPatient = globalPatientRepository.findByGoogleId(googleId);
        if (existingGlobalPatient.isPresent()) {
            // Global patient exists, create tenant-scoped patient
            GlobalPatientEntity globalPatient = existingGlobalPatient.get();
            PatientEntity newPatient = createTenantPatient(
                globalPatient,
                firstName,
                lastName,
                email,
                googleId,
                tenant
            );
            
            logger.info("Created tenant-scoped patient for existing global patient: patientId={}, globalPatientId={}", 
                       newPatient.getId(), globalPatient.getId());
            
            metricsService.recordNewPatientCreated(tenant.getSlug());
            
            return new PatientAuthResult(
                newPatient.getId(),
                globalPatient.getId(),
                email,
                firstName,
                lastName,
                true,   // new patient in this tenant
                false   // not account linking
            );
        }

        // 3. Check if patient exists with this email in this tenant (account linking scenario)
        Optional<PatientEntity> patientByEmail = patientRepository.findByTenantIdAndGlobalPatient_Email(tenantId, email);
        if (patientByEmail.isPresent()) {
            PatientEntity patient = patientByEmail.get();
            GlobalPatientEntity globalPatient = patient.getGlobalPatient();
            
            // Link Google account to existing patient
            if (globalPatient.hasGoogleAuth()) {
                // Already has a different Google account linked
                logger.warn("Patient already has Google account linked: patientId={}, existingGoogleId={}", 
                           patient.getId(), globalPatient.getGoogleId());
                metricsService.recordAccountLinkingFailure(tenant.getSlug(), "already_linked");
                throw new IllegalStateException("Patient already has a Google account linked");
            }
            
            // Link the Google account
            globalPatient.linkGoogleAccount(googleId, email);
            globalPatientRepository.save(globalPatient);
            
            patient.linkGoogleAccount(googleId);
            patient.markLogin();
            patientRepository.save(patient);
            
            logger.info("Linked Google account to existing patient: patientId={}, googleId={}", 
                       patient.getId(), googleId);
            
            metricsService.recordAccountLinkingSuccess(tenant.getSlug());
            
            return new PatientAuthResult(
                patient.getId(),
                globalPatient.getId(),
                email,
                patient.getFirstName(),
                patient.getLastName(),
                false,  // not a new patient
                true    // account was linked
            );
        }

        // 4. Create new global patient and tenant-scoped patient
        GlobalPatientEntity newGlobalPatient = new GlobalPatientEntity(googleId, email, firstName, lastName);
        newGlobalPatient = globalPatientRepository.save(newGlobalPatient);
        
        PatientEntity newPatient = createTenantPatient(
            newGlobalPatient,
            firstName,
            lastName,
            email,
            googleId,
            tenant
        );
        
        logger.info("Created new patient via Google OAuth: patientId={}, globalPatientId={}", 
                   newPatient.getId(), newGlobalPatient.getId());
        
        metricsService.recordNewPatientCreated(tenant.getSlug());
        
        return new PatientAuthResult(
            newPatient.getId(),
            newGlobalPatient.getId(),
            email,
            firstName,
            lastName,
            true,   // new patient
            false   // not account linking
        );
    }

    /**
     * Link Google account to existing patient.
     * This is used when a patient wants to add Google authentication to their existing account.
     * 
     * @param patientId Patient ID
     * @param googleId Google user ID
     * @param tenantId Tenant ID for scoping
     */
    @Transactional
    public void linkGoogleAccount(Long patientId, String googleId, Long tenantId) {
        logger.info("Linking Google account: patientId={}, googleId={}, tenantId={}", 
                   patientId, googleId, tenantId);

        // Find patient with tenant scoping
        PatientEntity patient = patientRepository.findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        GlobalPatientEntity globalPatient = patient.getGlobalPatient();

        // Get tenant slug for metrics
        String tenantSlug = patient.getTenant().getSlug();
        
        // Check if already linked
        if (globalPatient.hasGoogleAuth()) {
            metricsService.recordAccountLinkingFailure(tenantSlug, "already_linked");
            throw new IllegalStateException("Patient already has a Google account linked");
        }

        // Check if this Google ID is already used by another patient
        Optional<GlobalPatientEntity> existingGlobal = globalPatientRepository.findByGoogleId(googleId);
        if (existingGlobal.isPresent() && !existingGlobal.get().getId().equals(globalPatient.getId())) {
            metricsService.recordAccountLinkingFailure(tenantSlug, "google_id_in_use");
            throw new IllegalStateException("This Google account is already linked to another patient");
        }

        // Link the accounts
        globalPatient.linkGoogleAccount(googleId, globalPatient.getEmail());
        globalPatientRepository.save(globalPatient);

        patient.linkGoogleAccount(googleId);
        patientRepository.save(patient);

        logger.info("Successfully linked Google account: patientId={}, googleId={}", patientId, googleId);
        metricsService.recordAccountLinkingSuccess(tenantSlug);
    }

    /**
     * Find patient by Google ID and tenant.
     * This ensures proper tenant scoping for Google accounts.
     * 
     * @param googleId Google user ID
     * @param tenantId Tenant ID
     * @return Optional containing the patient if found
     */
    @Transactional(readOnly = true)
    public Optional<PatientEntity> findByGoogleIdAndTenant(String googleId, Long tenantId) {
        return patientRepository.findByGoogleIdAndTenantId(googleId, tenantId);
    }

    /**
     * Create a tenant-scoped patient entity.
     * Helper method to create a patient record for a specific tenant.
     */
    private PatientEntity createTenantPatient(
            GlobalPatientEntity globalPatient,
            String firstName,
            String lastName,
            String email,
            String googleId,
            TenantEntity tenant
    ) {
        PatientEntity patient = new PatientEntity(firstName, lastName, email, null);
        patient.setGlobalPatient(globalPatient);
        patient.setTenant(tenant);
        patient.setGoogleId(googleId);
        patient.markLogin();
        
        return patientRepository.save(patient);
    }
}
