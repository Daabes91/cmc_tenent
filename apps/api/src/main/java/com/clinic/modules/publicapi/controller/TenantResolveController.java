package com.clinic.modules.publicapi.controller;

import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class TenantResolveController {

    private final TenantService tenantService;

    public TenantResolveController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/public/tenant/resolve")
    public ResponseEntity<?> resolveTenant(@RequestParam(name = "domain", required = false) String domain) {
        String normalized = normalizeDomain(domain);
        if (!StringUtils.hasText(normalized)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid domain parameter"));
        }

        Optional<TenantEntity> tenantOpt = tenantService.findActiveByDomain(normalized);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Tenant not found"));
        }

        return ResponseEntity.ok(Map.of("slug", tenantOpt.get().getSlug()));
    }

    private String normalizeDomain(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value.trim().toLowerCase();

        if (cleaned.startsWith("https://")) {
            cleaned = cleaned.substring(8);
        } else if (cleaned.startsWith("http://")) {
            cleaned = cleaned.substring(7);
        }

        int slashIndex = cleaned.indexOf('/');
        if (slashIndex > -1) {
            cleaned = cleaned.substring(0, slashIndex);
        }

        int colonIndex = cleaned.indexOf(':');
        if (colonIndex > -1) {
            cleaned = cleaned.substring(0, colonIndex);
        }

        if (cleaned.startsWith("www.")) {
            cleaned = cleaned.substring(4);
        }

        return cleaned.isEmpty() ? null : cleaned;
    }
}
