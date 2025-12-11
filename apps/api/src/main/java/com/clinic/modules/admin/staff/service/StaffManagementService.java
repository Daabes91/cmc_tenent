package com.clinic.modules.admin.staff.service;

import com.clinic.modules.admin.staff.dto.*;
import com.clinic.modules.admin.staff.model.*;
import com.clinic.modules.admin.staff.repository.StaffInvitationTokenRepository;
import com.clinic.modules.admin.staff.repository.StaffPermissionsRepository;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.doctor.DoctorEntity;
import com.clinic.modules.core.doctor.DoctorRepository;
import com.clinic.modules.core.email.EmailService;
import com.clinic.modules.saas.config.PlanTierConfig;
import com.clinic.modules.saas.model.PlanTier;
import com.clinic.modules.saas.repository.SubscriptionRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.clinic.modules.core.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing staff members, their permissions, and invitations.
 */
@Service
public class StaffManagementService {

    private static final Logger log = LoggerFactory.getLogger(StaffManagementService.class);
    private static final Duration INVITATION_TOKEN_VALIDITY = Duration.ofDays(7);

    private final StaffUserRepository staffUserRepository;
    private final StaffPermissionsRepository staffPermissionsRepository;
    private final StaffInvitationTokenRepository invitationTokenRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PlanTierConfig planTierConfig;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantService tenantService;
    private final TenantContextHolder tenantContextHolder;

    @Value("${app.admin-url:http://localhost:3000}")
    private String adminUrl;

    public StaffManagementService(
            StaffUserRepository staffUserRepository,
            StaffPermissionsRepository staffPermissionsRepository,
            StaffInvitationTokenRepository invitationTokenRepository,
            DoctorRepository doctorRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            PlanTierConfig planTierConfig,
            SubscriptionRepository subscriptionRepository,
            TenantService tenantService,
            TenantContextHolder tenantContextHolder
    ) {
        this.staffUserRepository = staffUserRepository;
        this.staffPermissionsRepository = staffPermissionsRepository;
        this.invitationTokenRepository = invitationTokenRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.planTierConfig = planTierConfig;
        this.subscriptionRepository = subscriptionRepository;
        this.tenantService = tenantService;
        this.tenantContextHolder = tenantContextHolder;
    }

    /**
     * Create a new staff member and send an invitation email.
     */
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("Creating new staff member with email: {}", normalizedEmail);

        enforceStaffLimit(currentTenantId());

        // Check if email already exists
        if (staffUserRepository.findByEmailIgnoreCaseAndTenantId(normalizedEmail, currentTenantId()).isPresent()) {
            throw new IllegalArgumentException("Staff member with email " + request.email() + " already exists");
        }

        // Validate doctorId if provided
        if (request.doctorId() != null) {
            if (request.role() != StaffRole.DOCTOR) {
                throw new IllegalArgumentException("Only staff members with DOCTOR role can be linked to a doctor profile");
            }

            // Check if doctor exists
            DoctorEntity doctor = doctorRepository.findById(request.doctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Doctor with ID " + request.doctorId() + " not found"));

            // Check if doctor is already linked to another staff account
            if (staffUserRepository.findByDoctorIdAndTenantId(request.doctorId(), currentTenantId()).isPresent()) {
                throw new IllegalArgumentException("This doctor is already linked to another staff account");
            }
        }

        // Create staff user with INVITED status and a temporary password
        StaffUser staffUser = new StaffUser();
        staffUser.setEmail(normalizedEmail);
        staffUser.setFullName(request.fullName());
        staffUser.setRole(request.role());
        staffUser.setStatus(StaffStatus.INVITED);
        // Set a random temporary password that they can't use (will be replaced via invitation)
        staffUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        // Set doctor if provided
        if (request.doctorId() != null) {
            DoctorEntity doctor = doctorRepository.findById(request.doctorId()).orElseThrow();
            staffUser.setDoctor(doctor);
            log.info("Linked staff user to doctor ID: {}", request.doctorId());
        }

        staffUser.setTenant(tenantService.requireTenant(currentTenantId()));
        staffUser = staffUserRepository.save(staffUser);
        log.info("Staff user created with ID: {}", staffUser.getId());

        // Create permissions
        StaffPermissions permissions = createPermissionsFromDto(staffUser.getId(), request.permissions());
        staffPermissionsRepository.save(permissions);
        log.info("Permissions created for staff user ID: {}", staffUser.getId());

        // Generate and send invitation token
        String token = generateInvitationToken(staffUser.getId());
        sendInvitationEmail(staffUser, token);

        return mapToStaffResponse(staffUser, permissions, true);
    }

    /**
     * Get all staff members.
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        log.debug("Fetching all staff members");
        List<StaffUser> staffUsers = staffUserRepository.findAllByTenantId(currentTenantId());

        return staffUsers.stream()
                .map(this::mapToStaffResponseWithPermissions)
                .toList();
    }

    /**
     * Get a staff member by ID.
     */
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id) {
        log.debug("Fetching staff member with ID: {}", id);
        StaffUser staffUser = requireStaffUser(id);

        return mapToStaffResponseWithPermissions(staffUser);
    }

    /**
     * Update a staff member.
     */
    @Transactional
    public StaffResponse updateStaff(Long id, UpdateStaffRequest request) {
        log.info("Updating staff member with ID: {}", id);

        StaffUser staffUser = requireStaffUser(id);

        boolean updated = false;

        // Update email if provided
        if (request.email() != null && !request.email().equalsIgnoreCase(staffUser.getEmail())) {
            String normalizedEmail = request.email().trim().toLowerCase();
            // Check if new email is already in use
            if (staffUserRepository.existsByEmailIgnoreCaseAndIdNotAndTenantId(normalizedEmail, id, currentTenantId())) {
                throw new IllegalArgumentException("Email " + request.email() + " is already in use");
            }
            staffUser.setEmail(normalizedEmail);
            updated = true;
        }

        // Update full name if provided
        if (request.fullName() != null) {
            staffUser.setFullName(request.fullName());
            updated = true;
        }

        // Update role if provided
        if (request.role() != null) {
            staffUser.setRole(request.role());
            updated = true;
        }

        // Update status if provided
        if (request.status() != null) {
            staffUser.setStatus(request.status());
            updated = true;
        }

        // Update doctor if provided (can be null to unlink)
        if (request.doctorId() != null) {
            // Validate role is DOCTOR
            StaffRole currentRole = request.role() != null ? request.role() : staffUser.getRole();
            if (currentRole != StaffRole.DOCTOR) {
                throw new IllegalArgumentException("Only staff members with DOCTOR role can be linked to a doctor profile");
            }

            // Check if doctor exists
            DoctorEntity doctor = doctorRepository.findById(request.doctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Doctor with ID " + request.doctorId() + " not found"));

            // Check if doctor is already linked to another staff account (excluding current staff)
            staffUserRepository.findByDoctorIdAndTenantId(request.doctorId(), currentTenantId()).ifPresent(existingStaff -> {
                if (!existingStaff.getId().equals(id)) {
                    throw new IllegalArgumentException("This doctor is already linked to another staff account");
                }
            });

            staffUser.setDoctor(doctor);
            updated = true;
            log.info("Updated staff user {} link to doctor ID: {}", id, request.doctorId());
        }

        if (updated) {
            staffUser = staffUserRepository.save(staffUser);
            log.info("Staff user updated: {}", staffUser.getId());
        }

        // Update permissions if provided
        if (request.permissions() != null) {
            updateStaffPermissions(staffUser.getId(), request.permissions());
            log.info("Permissions updated for staff user ID: {}", staffUser.getId());
        }

        return mapToStaffResponseWithPermissions(staffUser);
    }

    /**
     * Delete a staff member.
     */
    @Transactional
    public void deleteStaff(Long id) {
        log.info("Deleting staff member with ID: {}", id);

        StaffUser staffUser = requireStaffUser(id);

        // Delete permissions (will cascade)
        staffPermissionsRepository.deleteByStaffUserId(staffUser.getId());

        // Delete invitation tokens (will cascade)
        invitationTokenRepository.deleteByStaffUserId(staffUser.getId());

        // Delete staff user
        staffUserRepository.delete(staffUser);

        log.info("Staff member deleted: {}", staffUser.getId());
    }

    /**
     * Resend invitation email to a staff member.
     */
    @Transactional
    public void resendInvitation(Long staffId) {
        log.info("Resending invitation to staff ID: {}", staffId);

        StaffUser staffUser = requireStaffUser(staffId);

        if (staffUser.getStatus() == StaffStatus.ACTIVE) {
            throw new IllegalStateException("Cannot send invitation to active staff member");
        }

        // Generate new token
        String token = generateInvitationToken(staffUser.getId());
        sendInvitationEmail(staffUser, token);

        log.info("Invitation resent to: {}", staffUser.getEmail());
    }

    /**
     * Set password using invitation token.
     */
    @Transactional
    public void setPasswordWithToken(SetPasswordRequest request) {
        log.info("Setting password with invitation token");

        // Find and validate token
        StaffInvitationToken invitationToken = invitationTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (!invitationToken.isValid()) {
            throw new IllegalArgumentException("Invitation token is expired or already used");
        }

        // Get staff user
        StaffUser staffUser = staffUserRepository.findById(invitationToken.getStaffUserId())
                .orElseThrow(() -> new IllegalStateException("Staff user not found"));

        // Update password and activate
        staffUser.setPasswordHash(passwordEncoder.encode(request.password()));
        staffUser.setStatus(StaffStatus.ACTIVE);
        staffUserRepository.save(staffUser);

        // Mark token as used
        invitationToken.markAsUsed();
        invitationTokenRepository.save(invitationToken);

        log.info("Password set and staff activated: {}", staffUser.getEmail());
    }

    /**
     * Get permissions for a staff member.
     */
    @Transactional(readOnly = true)
    public ModulePermissionsDto getStaffPermissions(Long staffId) {
        requireStaffUser(staffId);
        StaffPermissions permissions = staffPermissionsRepository.findByStaffUserId(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Permissions not found for staff member"));

        return mapToModulePermissionsDto(permissions);
    }

    /**
     * Update permissions for a staff member.
     */
    @Transactional
    public void updateStaffPermissions(Long staffId, ModulePermissionsDto permissionsDto) {
        requireStaffUser(staffId);
        StaffPermissions permissions = staffPermissionsRepository.findByStaffUserId(staffId)
                .orElseGet(() -> new StaffPermissions(staffId));

        permissions.setAppointmentsPermissions(permissionsDto.appointments());
        permissions.setCalendarPermissions(permissionsDto.calendar());
        permissions.setPatientsPermissions(permissionsDto.patients());
        permissions.setDoctorsPermissions(permissionsDto.doctors());
        permissions.setMaterialsPermissions(permissionsDto.materials());
        permissions.setServicesPermissions(permissionsDto.services());
        permissions.setInsuranceCompaniesPermissions(permissionsDto.insuranceCompanies());
        permissions.setTreatmentPlansPermissions(permissionsDto.treatmentPlans());
        permissions.setReportsPermissions(permissionsDto.reports());
        permissions.setBillingPermissions(permissionsDto.billing());
        permissions.setTranslationsPermissions(permissionsDto.translations());
        permissions.setSettingsPermissions(permissionsDto.settings());
        permissions.setClinicSettingsPermissions(permissionsDto.clinicSettings());
        permissions.setStaffPermissions(permissionsDto.staff());
        permissions.setBlogsPermissions(permissionsDto.blogs());

        staffPermissionsRepository.save(permissions);
    }

    // Private helper methods

    private StaffPermissions createPermissionsFromDto(Long staffUserId, ModulePermissionsDto dto) {
        StaffPermissions permissions = new StaffPermissions(staffUserId);
        permissions.setAppointmentsPermissions(dto.appointments());
        permissions.setCalendarPermissions(dto.calendar());
        permissions.setPatientsPermissions(dto.patients());
        permissions.setDoctorsPermissions(dto.doctors());
        permissions.setMaterialsPermissions(dto.materials());
        permissions.setServicesPermissions(dto.services());
        permissions.setInsuranceCompaniesPermissions(dto.insuranceCompanies());
        permissions.setTreatmentPlansPermissions(dto.treatmentPlans());
        permissions.setReportsPermissions(dto.reports());
        permissions.setBillingPermissions(dto.billing());
        permissions.setTranslationsPermissions(dto.translations());
        permissions.setSettingsPermissions(dto.settings());
        permissions.setClinicSettingsPermissions(dto.clinicSettings());
        permissions.setStaffPermissions(dto.staff());
        permissions.setBlogsPermissions(dto.blogs());
        return permissions;
    }

    private String generateInvitationToken(Long staffUserId) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(INVITATION_TOKEN_VALIDITY);

        StaffInvitationToken invitationToken = new StaffInvitationToken(staffUserId, token, expiresAt);
        invitationTokenRepository.save(invitationToken);

        return token;
    }

    private void sendInvitationEmail(StaffUser staffUser, String token) {
        String setupUrl = adminUrl + "/set-password?token=" + token;
        emailService.sendStaffInvitation(
                staffUser.getEmail(),
                staffUser.getFullName(),
                setupUrl,
                INVITATION_TOKEN_VALIDITY.toDays()
        );
    }

    private StaffResponse mapToStaffResponseWithPermissions(StaffUser staffUser) {
        StaffPermissions permissions = staffPermissionsRepository.findByStaffUserId(staffUser.getId())
                .orElse(new StaffPermissions(staffUser.getId()));

        boolean hasPendingInvitation = invitationTokenRepository
                .findLatestUnusedTokenByStaffUserId(staffUser.getId())
                .map(StaffInvitationToken::isValid)
                .orElse(false);

        return mapToStaffResponse(staffUser, permissions, hasPendingInvitation);
    }

    private StaffResponse mapToStaffResponse(StaffUser staffUser, StaffPermissions permissions, boolean hasPendingInvitation) {
        StaffResponse.DoctorInfo doctorInfo = null;
        if (staffUser.getDoctor() != null) {
            DoctorEntity doctor = staffUser.getDoctor();
            doctorInfo = new StaffResponse.DoctorInfo(
                    doctor.getId(),
                    doctor.getFullName(),
                    doctor.getSpecialty()
            );
        }

        return new StaffResponse(
                staffUser.getId(),
                staffUser.getEmail(),
                staffUser.getFullName(),
                staffUser.getRole(),
                staffUser.getStatus(),
                mapToModulePermissionsDto(permissions),
                hasPendingInvitation,
                staffUser.getCreatedAt(),
                doctorInfo
        );
    }

    private ModulePermissionsDto mapToModulePermissionsDto(StaffPermissions permissions) {
        return new ModulePermissionsDto(
                permissions.getAppointmentsPermissions(),
                permissions.getCalendarPermissions(),
                permissions.getPatientsPermissions(),
                permissions.getDoctorsPermissions(),
                permissions.getMaterialsPermissions(),
                permissions.getServicesPermissions(),
                permissions.getInsuranceCompaniesPermissions(),
                permissions.getTreatmentPlansPermissions(),
                permissions.getReportsPermissions(),
                permissions.getBillingPermissions(),
                permissions.getTranslationsPermissions(),
                permissions.getSettingsPermissions(),
                permissions.getClinicSettingsPermissions(),
                permissions.getStaffPermissions(),
                permissions.getBlogsPermissions()
        );
    }

    /**
     * Cleanup expired invitation tokens (should be called periodically).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired invitation tokens");
        invitationTokenRepository.deleteExpiredTokens(Instant.now());
    }

    private StaffUser requireStaffUser(Long id) {
        return staffUserRepository.findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found"));
    }

    private Long currentTenantId() {
        return tenantContextHolder.requireTenantId();
    }

    /**
     * Enforce staff limit based on the tenant's active plan.
     */
    private void enforceStaffLimit(Long tenantId) {
        int maxStaff = resolveMaxStaff(tenantId);
        if (maxStaff < 0) {
            return; // Unlimited
        }
        long currentCount = staffUserRepository.countByTenantId(tenantId);
        if (currentCount >= maxStaff) {
            PlanTier tier = resolvePlanTier(tenantId);
            String planName = tier != null ? tier.name() : "UNKNOWN";
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Staff limit reached for plan " + planName + " (" + maxStaff + " staff allowed)"
            );
        }
    }

    private int resolveMaxStaff(Long tenantId) {
        PlanTier tier = resolvePlanTier(tenantId);
        PlanTierConfig.PlanTierDetails details = tier != null ? planTierConfig.getPlanDetails(tier) : null;
        if (details == null) {
            return -1;
        }
        return details.getMaxStaff();
    }

    private PlanTier resolvePlanTier(Long tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
                .map(subscription -> subscription.getPlanTier() != null ? subscription.getPlanTier() : PlanTier.BASIC)
                .orElse(PlanTier.BASIC);
    }
}
