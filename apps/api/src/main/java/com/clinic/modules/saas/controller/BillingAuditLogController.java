package com.clinic.modules.saas.controller;

import com.clinic.modules.saas.dto.BillingAuditLogDto;
import com.clinic.modules.saas.service.BillingAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saas/billing/audit-logs")
@Tag(name = "Billing Audit Logs", description = "Endpoints for viewing billing audit trail")
@SecurityRequirement(name = "bearerAuth")
public class BillingAuditLogController {

    private final BillingAuditLogService billingAuditLogService;

    public BillingAuditLogController(BillingAuditLogService billingAuditLogService) {
        this.billingAuditLogService = billingAuditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SAAS_MANAGER')")
    @Operation(
            summary = "Get billing audit logs",
            description = "Retrieve paginated billing audit log entries with optional filtering"
    )
    public ResponseEntity<Page<BillingAuditLogDto>> getAuditLogs(
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "actionType", required = false) String actionType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<BillingAuditLogDto> result = billingAuditLogService.getAuditLogs(
                tenantId,
                actionType,
                PageRequest.of(safePage, safeSize)
        );

        return ResponseEntity.ok(result);
    }
}
