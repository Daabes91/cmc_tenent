package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.ServiceSummaryResponse;
import com.clinic.modules.admin.dto.ServiceUpsertRequest;
import com.clinic.modules.admin.service.ServiceAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/services")
public class ServiceAdminController {

    private final ServiceAdminService serviceAdminService;

    public ServiceAdminController(ServiceAdminService serviceAdminService) {
        this.serviceAdminService = serviceAdminService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.canView('services')")
    public ResponseEntity<ApiResponse<List<ServiceSummaryResponse>>> listServices() {
        List<ServiceSummaryResponse> services = serviceAdminService.listServices();
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "SERVICES_LISTED",
                        "Services fetched successfully.",
                        services,
                        Map.of("count", services.size()),
                        null
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.canView('services')")
    public ResponseEntity<ApiResponse<ServiceSummaryResponse>> getService(@PathVariable Long id) {
        ServiceSummaryResponse response = serviceAdminService.getService(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "SERVICE_RETRIEVED",
                        "Service fetched successfully.",
                        response
                )
        );
    }

    @PostMapping
    @PreAuthorize("@permissionService.canCreate('services')")
    public ResponseEntity<ApiResponse<ServiceSummaryResponse>> createService(@Valid @RequestBody ServiceUpsertRequest request) {
        ServiceSummaryResponse response = serviceAdminService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseFactory.success(
                        "SERVICE_CREATED",
                        "Service created successfully.",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit('services')")
    public ResponseEntity<ApiResponse<ServiceSummaryResponse>> updateService(@PathVariable Long id,
                                                                             @Valid @RequestBody ServiceUpsertRequest request) {
        ServiceSummaryResponse response = serviceAdminService.updateService(id, request);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "SERVICE_UPDATED",
                        "Service updated successfully.",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.canDelete('services')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        serviceAdminService.deleteService(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "SERVICE_DELETED",
                        "Service deleted successfully."
                )
        );
    }
}
