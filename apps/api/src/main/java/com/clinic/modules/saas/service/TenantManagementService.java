package com.clinic.modules.saas.service;

import com.clinic.modules.admin.staff.model.*;
import com.clinic.modules.admin.staff.repository.StaffPermissionsRepository;
import com.clinic.modules.admin.staff.repository.StaffUserRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantRepository;
import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.exception.ConflictException;
import com.clinic.modules.saas.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing tenant CRUD operations with auto-provisioning.
 * Handles tenant creation, retrieval, updates, and soft deletion.
 */
@Service
public class TenantManagementService {

    private static final Logger log = LoggerFactory.getLogger(TenantManagementService.class);
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 16;

    private final TenantRepository tenantRepository;
    private final StaffUserRepository staffUserRepository;
    private final StaffPermissionsRepository staffPermissionsRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public TenantManagementService(
            TenantRepository tenantRepository,
            StaffUserRepository staffUserRepository,
            StaffPermissionsRepository staffPermissionsRepository,
            PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.staffUserRepository = staffUserRepository;
        this.staffPermissionsRepository = staffPermissionsRepository;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Create a new tenant with auto-provisioned admin staff and full permissions.
     * This operation is transactional - if any step fails, the entire operation rolls back.
     *
     * @param request Tenant creation request
     * @return Response containing tenant details and admin credentials
     * @throws ConflictException if tenant slug already exists
     */
    @Transactional
    public TenantCreateResponse createTenant(TenantCreateRequest request) {
        log.info("Tenant creation initiated - slug: {}, name: {}, customDomain: {}", 
                request.slug(), request.name(), request.customDomain());

        // 1. Validate slug uniqueness
        if (tenantRepository.findBySlugIgnoreCaseAndNotDeleted(request.slug()).isPresent()) {
            log.warn("Tenant creation failed - slug already exists: {}", request.slug());
            throw new ConflictException("Tenant with slug '" + request.slug() + "' already exists");
        }

        // 2. Create tenant
        TenantEntity tenant = new TenantEntity(request.slug(), request.name());
        tenant.setCustomDomain(request.customDomain());
        tenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully - tenantId: {}, slug: {}, status: {}", 
                tenant.getId(), tenant.getSlug(), tenant.getStatus());

        // 3. Generate admin credentials
        String adminEmail = generateAdminEmail(request.slug());
        String rawPassword = generateSecurePassword();
        String passwordHash = passwordEncoder.encode(rawPassword);

        // 4. Create admin staff
        StaffUser adminStaff = new StaffUser(
                adminEmail,
                "Admin",
                StaffRole.ADMIN,
                passwordHash,
                StaffStatus.ACTIVE
        );
        adminStaff.setTenant(tenant);
        adminStaff = staffUserRepository.save(adminStaff);
        log.info("Admin staff auto-provisioned - staffId: {}, tenantId: {}, email: {}, role: {}", 
                adminStaff.getId(), tenant.getId(), adminEmail, StaffRole.ADMIN);

        // 5. Grant full permissions
        StaffPermissions permissions = createFullPermissions(adminStaff.getId());
        staffPermissionsRepository.save(permissions);
        log.info("Full permissions granted - staffId: {}, tenantId: {}, modules: ALL, actions: ALL", 
                adminStaff.getId(), tenant.getId());

        // Log successful tenant creation with masked password
        log.info("Tenant creation completed successfully - tenantId: {}, slug: {}, adminEmail: {}, passwordGenerated: [MASKED]", 
                tenant.getId(), tenant.getSlug(), adminEmail);

        // 6. Return response with credentials (password is only returned once!)
        AdminCredentials adminCredentials = new AdminCredentials(adminEmail, rawPassword);

        return new TenantCreateResponse(
                tenant.getId(),
                tenant.getSlug(),
                tenant.getName(),
                tenant.getCustomDomain(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                adminCredentials
        );
    }

    /**
     * Generate admin email using pattern: admin@{slug}.clinic.local
     */
    private String generateAdminEmail(String slug) {
        return String.format("admin@%s.clinic.local", slug);
    }

    /**
     * Generate a secure 16-character password with uppercase, lowercase, digits, and special characters.
     */
    private String generateSecurePassword() {
        return secureRandom.ints(PASSWORD_LENGTH, 0, PASSWORD_CHARS.length())
                .mapToObj(PASSWORD_CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    /**
     * Create full permissions for all modules with all actions.
     */
    private StaffPermissions createFullPermissions(Long staffUserId) {
        StaffPermissions permissions = new StaffPermissions(staffUserId);

        // Grant all permissions for all modules
        Set<PermissionAction> allActions = Set.of(
                PermissionAction.VIEW,
                PermissionAction.CREATE,
                PermissionAction.EDIT,
                PermissionAction.DELETE
        );

        for (ModuleName module : ModuleName.values()) {
            permissions.setPermissionsForModule(module, allActions);
        }

        return permissions;
    }

    /**
     * Retrieve a tenant by ID with optional inclusion of soft-deleted tenants.
     *
     * @param id Tenant ID
     * @param includeDeleted Whether to include soft-deleted tenants
     * @return Tenant response
     * @throws NotFoundException if tenant not found
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenant(Long id, boolean includeDeleted) {
        log.debug("Tenant retrieval requested - tenantId: {}, includeDeleted: {}", id, includeDeleted);

        TenantEntity tenant = tenantRepository.findByIdWithDeletedFilter(id, includeDeleted)
                .orElseThrow(() -> {
                    log.warn("Tenant retrieval failed - tenant not found: tenantId: {}", id);
                    return new NotFoundException("Tenant with ID " + id + " not found");
                });

        log.debug("Tenant retrieved successfully - tenantId: {}, slug: {}, status: {}, deleted: {}", 
                tenant.getId(), tenant.getSlug(), tenant.getStatus(), tenant.isDeleted());

        return mapToTenantResponse(tenant);
    }

    /**
     * List all tenants with pagination and optional inclusion of soft-deleted tenants.
     *
     * @param pageable Pagination parameters
     * @param includeDeleted Whether to include soft-deleted tenants
     * @return Paginated list of tenants
     */
    @Transactional(readOnly = true)
    public TenantListResponse listTenants(Pageable pageable, boolean includeDeleted) {
        log.debug("Tenant list requested - page: {}, size: {}, includeDeleted: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), includeDeleted);

        Page<TenantEntity> page = tenantRepository.findAllWithDeletedFilter(includeDeleted, pageable);

        log.debug("Tenant list retrieved - totalElements: {}, totalPages: {}, currentPage: {}", 
                page.getTotalElements(), page.getTotalPages(), page.getNumber());

        return new TenantListResponse(
                page.getContent().stream()
                        .map(this::mapToTenantResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Update an existing tenant.
     * Slug cannot be modified after creation.
     *
     * @param id Tenant ID
     * @param request Update request
     * @return Updated tenant response
     * @throws NotFoundException if tenant not found or is soft-deleted
     */
    @Transactional
    public TenantResponse updateTenant(Long id, TenantUpdateRequest request) {
        log.info("Tenant update initiated - tenantId: {}, name: {}, customDomain: {}", 
                id, request.name(), request.customDomain());

        // Retrieve tenant (exclude soft-deleted)
        TenantEntity tenant = tenantRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.warn("Tenant update failed - tenant not found or deleted: tenantId: {}", id);
                    return new NotFoundException("Tenant with ID " + id + " not found");
                });

        String oldName = tenant.getName();
        String oldCustomDomain = tenant.getCustomDomain();

        // Update fields if provided
        if (request.name() != null && !request.name().isBlank()) {
            tenant.setName(request.name());
        }

        if (request.customDomain() != null) {
            tenant.setCustomDomain(request.customDomain().isBlank() ? null : request.customDomain());
        }

        tenant = tenantRepository.save(tenant);
        log.info("Tenant updated successfully - tenantId: {}, slug: {}, nameChanged: {}, customDomainChanged: {}", 
                tenant.getId(), tenant.getSlug(), 
                !oldName.equals(tenant.getName()), 
                !java.util.Objects.equals(oldCustomDomain, tenant.getCustomDomain()));

        return mapToTenantResponse(tenant);
    }

    /**
     * Soft delete a tenant by setting deletedAt timestamp and status to INACTIVE.
     *
     * @param id Tenant ID
     * @throws NotFoundException if tenant not found or already deleted
     */
    @Transactional
    public void softDeleteTenant(Long id) {
        log.info("Tenant soft delete initiated - tenantId: {}", id);

        // Retrieve tenant (exclude already deleted)
        TenantEntity tenant = tenantRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.warn("Tenant soft delete failed - tenant not found or already deleted: tenantId: {}", id);
                    return new NotFoundException("Tenant with ID " + id + " not found");
                });

        String slug = tenant.getSlug();
        String name = tenant.getName();
        
        tenant.softDelete();
        tenantRepository.save(tenant);
        
        log.info("Tenant soft deleted successfully - tenantId: {}, slug: {}, name: {}, deletedAt: {}, newStatus: {}", 
                tenant.getId(), slug, name, tenant.getDeletedAt(), tenant.getStatus());
    }

    /**
     * Map TenantEntity to TenantResponse DTO.
     */
    private TenantResponse mapToTenantResponse(TenantEntity tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getSlug(),
                tenant.getName(),
                tenant.getCustomDomain(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt(),
                tenant.getDeletedAt()
        );
    }
}
