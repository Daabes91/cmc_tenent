package com.clinic.modules.core.tenant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-facing endpoint to expose the current tenant context (id, slug, feature flags)
 * derived from the authenticated staff session.
 */
@RestController
@RequestMapping("/admin/tenant")
public class AdminTenantController {

    private final TenantContextHolder tenantContextHolder;
    private final TenantRepository tenantRepository;

    public AdminTenantController(TenantContextHolder tenantContextHolder,
                                 TenantRepository tenantRepository) {
        this.tenantContextHolder = tenantContextHolder;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<TenantInfoResponse> getCurrentTenant() {
        Long tenantId = tenantContextHolder.requireTenantId();
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        TenantInfoResponse response = new TenantInfoResponse(
                tenant.getId(),
                tenant.getSlug(),
                tenant.isEcommerceEnabled()
        );
        return ResponseEntity.ok(response);
    }

    public record TenantInfoResponse(Long id, String slug, boolean ecommerceEnabled) {}
}
