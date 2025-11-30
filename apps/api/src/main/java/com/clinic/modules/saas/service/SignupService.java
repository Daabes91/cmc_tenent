package com.clinic.modules.saas.service;

import com.clinic.modules.admin.staff.model.StaffRole;
import com.clinic.modules.admin.staff.model.StaffStatus;
import com.clinic.modules.admin.staff.model.StaffUser;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.BillingStatus;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.core.tenant.TenantStatus;
import com.clinic.modules.saas.dto.SignupRequest;
import com.clinic.modules.saas.dto.SignupResponse;
import com.clinic.modules.saas.exception.PayPalApiException;
import com.clinic.modules.saas.exception.PayPalConfigurationException;
import com.clinic.modules.saas.exception.SubdomainExistsException;
import com.clinic.modules.saas.model.PlanTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling clinic signup operations.
 * Creates tenants and owner users during the signup process.
 */
@Service
public class SignupService {

    private static final Logger logger = LoggerFactory.getLogger(SignupService.class);

    private final TenantRepository tenantRepository;
    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private final BillingAuditLogger auditLogger;

    public SignupService(
            TenantRepository tenantRepository,
            StaffUserRepository staffUserRepository,
            PasswordEncoder passwordEncoder,
            SubscriptionService subscriptionService,
            BillingAuditLogger auditLogger) {
        this.tenantRepository = tenantRepository;
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.auditLogger = auditLogger;
    }

    /**
     * Complete signup flow: create tenant, owner user, and PayPal subscription.
     *
     * @param request SignupRequest with clinic and owner information
     * @return SignupResponse with approval URL or error
     * @throws SubdomainExistsException if subdomain is already taken
     * @throws PayPalApiException if PayPal subscription creation fails
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        logger.info("Starting signup process for subdomain: {}", request.getSubdomain());
        auditLogger.logSuccess("SIGNUP_STARTED", null, null, 
            "Subdomain: " + request.getSubdomain() + ", Email: " + request.getEmail());

        try {
            // Validate subdomain availability
            if (!isSubdomainAvailable(request.getSubdomain())) {
                logger.warn("Subdomain already exists: {}", request.getSubdomain());
                auditLogger.logFailure("SIGNUP", null, null, 
                    "Subdomain already exists: " + request.getSubdomain(), null);
                throw new SubdomainExistsException(request.getSubdomain());
            }

            // Create tenant with pending_payment status
            TenantEntity tenant = createPendingTenant(request);
            logger.info("Created tenant with ID: {} for subdomain: {}", tenant.getId(), tenant.getSlug());
            auditLogger.logSuccess("TENANT_CREATED", tenant.getId(), null, 
                "Subdomain: " + tenant.getSlug() + ", Status: PENDING_PAYMENT");

            // Create owner user
            StaffUser ownerUser = createOwnerUser(tenant, request);
            logger.info("Created owner user with ID: {} for tenant: {}", ownerUser.getId(), tenant.getId());
            auditLogger.logSuccess("OWNER_USER_CREATED", tenant.getId(), ownerUser.getId(), 
                "Email: " + ownerUser.getEmail() + ", Role: ADMIN");

            // Create PayPal subscription
            PlanTier selectedTier = request.getPlanTier() != null ? request.getPlanTier() : PlanTier.BASIC;
            String billingCycle = request.getBillingCycle() != null ? request.getBillingCycle().toUpperCase() : "MONTHLY";

            String approvalUrl = subscriptionService.createSubscription(
                    tenant.getId(),
                    selectedTier,
                    billingCycle,
                    request.getClientBaseUrl());
            logger.info("Created PayPal subscription for tenant: {}, approval URL generated", tenant.getId());
            auditLogger.logSuccess("SIGNUP_COMPLETED", tenant.getId(), ownerUser.getId(), 
                "Approval URL generated, awaiting payment");

            return SignupResponse.success(approvalUrl, tenant.getId());

        } catch (SubdomainExistsException e) {
            auditLogger.logFailure("SIGNUP", null, null, 
                "Subdomain conflict: " + request.getSubdomain(), e);
            throw e;
        } catch (PayPalConfigurationException e) {
            auditLogger.logFailure("SIGNUP", null, null,
                "PayPal configuration error for subdomain: " + request.getSubdomain(), e);
            throw e;
        } catch (PayPalApiException e) {
            auditLogger.logFailure("SIGNUP", null, null, 
                "PayPal API error during signup for subdomain: " + request.getSubdomain(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during signup for subdomain: {}", request.getSubdomain(), e);
            auditLogger.logFailure("SIGNUP", null, null, 
                "Unexpected error for subdomain: " + request.getSubdomain(), e);
            throw new RuntimeException("Signup failed due to unexpected error", e);
        }
    }

    /**
     * Check if a subdomain is available.
     *
     * @param subdomain the subdomain to check
     * @return true if available, false if already taken
     */
    public boolean isSubdomainAvailable(String subdomain) {
        return tenantRepository.findBySlugIgnoreCase(subdomain).isEmpty();
    }

    /**
     * Create a new tenant with pending_payment billing status.
     *
     * @param request SignupRequest with clinic information
     * @return created TenantEntity
     */
    @Transactional
    public TenantEntity createPendingTenant(SignupRequest request) {
        TenantEntity tenant = new TenantEntity(
                request.getSubdomain().toLowerCase(),
                request.getClinicName()
        );
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setBillingStatus(BillingStatus.PENDING_PAYMENT);

        return tenantRepository.save(tenant);
    }

    /**
     * Create an owner user for the tenant.
     *
     * @param tenant the tenant entity
     * @param request SignupRequest with owner information
     * @return created StaffUser
     */
    @Transactional
    public StaffUser createOwnerUser(TenantEntity tenant, SignupRequest request) {
        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create owner user with ADMIN role
        StaffUser ownerUser = new StaffUser(
                request.getEmail().toLowerCase(),
                request.getOwnerName(),
                StaffRole.ADMIN,
                hashedPassword,
                StaffStatus.ACTIVE
        );
        ownerUser.setTenant(tenant);

        return staffUserRepository.save(ownerUser);
    }
}
