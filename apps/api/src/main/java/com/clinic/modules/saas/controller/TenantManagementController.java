package com.clinic.modules.saas.controller;

import com.clinic.modules.core.tenant.TenantStatus;
import com.clinic.modules.saas.dto.*;
import com.clinic.modules.saas.service.SubscriptionService;
import com.clinic.modules.saas.service.TenantManagementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for tenant management operations.
 * All endpoints require SAAS_MANAGER role authentication.
 */
@RestController
@RequestMapping("/saas/tenants")
public class TenantManagementController {

    private static final Logger log = LoggerFactory.getLogger(TenantManagementController.class);

    private final TenantManagementService tenantManagementService;
    private final SubscriptionService subscriptionService;

    public TenantManagementController(
            TenantManagementService tenantManagementService,
            SubscriptionService subscriptionService
    ) {
        this.tenantManagementService = tenantManagementService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Create a new tenant with auto-provisioned admin staff and full permissions.
     *
     * @param request Tenant creation request with slug, name, and optional custom domain
     * @return TenantCreateResponse with tenant details and admin credentials (201 Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<TenantCreateResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        log.info("API request received - POST /saas/tenants - slug: {}", request.slug());
        TenantCreateResponse response = tenantManagementService.createTenant(request);
        log.info("API response sent - POST /saas/tenants - status: 201, tenantId: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all tenants with pagination and optional filters.
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param includeDeleted Whether to include soft-deleted tenants (default: false)
     * @param status Optional status filter (ACTIVE, INACTIVE, SUSPENDED). Null or omitted returns all statuses.
     * @param billingStatus Optional billing status filter. Null or omitted returns all billing statuses.
     * @param planTier Optional plan tier filter. Null or omitted returns all plan tiers.
     * @return Paginated list of tenants (200 OK)
     */
    @GetMapping
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<TenantListResponse> listTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) TenantStatus status,
            @RequestParam(required = false) String billingStatus,
            @RequestParam(required = false) String planTier) {
        log.debug("Listing tenants - page: {}, size: {}, includeDeleted: {}, status: {}, billingStatus: {}, planTier: {}",
                page, size, includeDeleted, status, billingStatus, planTier);
        Pageable pageable = PageRequest.of(page, size);
        TenantListResponse response = tenantManagementService.listTenants(pageable, includeDeleted, status, billingStatus, planTier);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific tenant by ID.
     *
     * @param id Tenant ID
     * @param includeDeleted Whether to include soft-deleted tenants (default: false)
     * @return Tenant details (200 OK)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<TenantResponse> getTenant(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        log.debug("Retrieving tenant with ID: {}, includeDeleted: {}", id, includeDeleted);
        TenantResponse response = tenantManagementService.getTenant(id, includeDeleted);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing tenant.
     * Note: Slug cannot be modified after creation.
     *
     * @param id Tenant ID
     * @param request Update request with optional name, custom domain, and status
     * @return Updated tenant details (200 OK)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody TenantUpdateRequest request) {
        log.info("API request received - PUT /saas/tenants/{} - name: {}, customDomain: {}, status: {}",
                id, request.name(), request.customDomain(), request.status());
        TenantResponse response = tenantManagementService.updateTenant(id, request);
        log.info("API response sent - PUT /saas/tenants/{} - status: 200", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete a tenant.
     * Sets deletedAt timestamp and marks tenant as INACTIVE.
     *
     * @param id Tenant ID
     * @return No content (204 No Content)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        log.info("API request received - DELETE /saas/tenants/{}", id);
        tenantManagementService.softDeleteTenant(id);
        log.info("API response sent - DELETE /saas/tenants/{} - status: 204", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get subscription details for a tenant.
     *
     * @param id Tenant ID
     * @return Subscription details (200 OK) or 404 if no subscription exists
     */
    @GetMapping("/{id}/subscription")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<SubscriptionResponse> getTenantSubscription(@PathVariable Long id) {
        log.debug("Retrieving subscription for tenant ID: {}", id);
        SubscriptionResponse response = tenantManagementService.getTenantSubscription(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Manually override billing status for a tenant.
     * This action is logged for audit purposes.
     *
     * @param id Tenant ID
     * @param request Override request with new billing status and reason
     * @return No content (204 No Content)
     */
    @PutMapping("/{id}/billing-status")
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    public ResponseEntity<Void> updateBillingStatus(
            @PathVariable Long id,
            @Valid @RequestBody BillingStatusOverrideRequest request) {
        log.info("API request received - PUT /saas/tenants/{}/billing-status - status: {}, reason: {}",
                id, request.getBillingStatus(), request.getReason());
        // Ensure body reflects the path tenant ID for downstream logging/auditing
        request.setTenantId(id);
        tenantManagementService.updateBillingStatus(id, request);
        log.info("API response sent - PUT /saas/tenants/{}/billing-status - status: 204", id);
        return ResponseEntity.noContent().build();
    }
}
