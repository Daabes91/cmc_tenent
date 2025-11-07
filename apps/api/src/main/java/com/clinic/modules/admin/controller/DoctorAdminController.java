package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.DoctorAdminResponse;
import com.clinic.modules.admin.dto.DoctorUpsertRequest;
import com.clinic.modules.admin.service.DoctorAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/doctors")
public class DoctorAdminController {

    private final DoctorAdminService doctorAdminService;

    public DoctorAdminController(DoctorAdminService doctorAdminService) {
        this.doctorAdminService = doctorAdminService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.canView('doctors')")
    public ResponseEntity<ApiResponse<List<DoctorAdminResponse>>> listDoctors() {
        List<DoctorAdminResponse> doctors = doctorAdminService.listDoctors();
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTORS_LISTED",
                        "Doctors fetched successfully.",
                        doctors,
                        Map.of("count", doctors.size()),
                        null
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.canView('doctors')")
    public ResponseEntity<ApiResponse<DoctorAdminResponse>> getDoctor(@PathVariable Long id) {
        DoctorAdminResponse response = doctorAdminService.getDoctor(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTOR_RETRIEVED",
                        "Doctor fetched successfully.",
                        response
                )
        );
    }

    @PostMapping
    @PreAuthorize("@permissionService.canCreate('doctors')")
    public ResponseEntity<ApiResponse<DoctorAdminResponse>> createDoctor(@Valid @RequestBody DoctorUpsertRequest request) {
        DoctorAdminResponse response = doctorAdminService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseFactory.success(
                        "DOCTOR_CREATED",
                        "Doctor created successfully.",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit('doctors')")
    public ResponseEntity<ApiResponse<DoctorAdminResponse>> updateDoctor(@PathVariable Long id,
                                                                         @Valid @RequestBody DoctorUpsertRequest request) {
        DoctorAdminResponse response = doctorAdminService.updateDoctor(id, request);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTOR_UPDATED",
                        "Doctor updated successfully.",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.canDelete('doctors')")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorAdminService.deleteDoctor(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTOR_DELETED",
                        "Doctor deleted successfully."
                )
        );
    }
}
