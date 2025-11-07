package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.PatientAdminResponse;
import com.clinic.modules.admin.dto.PatientUpsertRequest;
import com.clinic.modules.admin.service.PatientAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/patients")
public class PatientAdminController {

    private final PatientAdminService patientAdminService;

    public PatientAdminController(PatientAdminService patientAdminService) {
        this.patientAdminService = patientAdminService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.canView('patients')")
    public ResponseEntity<ApiResponse<Page<PatientAdminResponse>>> listPatients(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "lastName") String sortBy,
            @RequestParam(name = "direction", required = false, defaultValue = "ASC") String direction) {

        // Create pageable with sorting
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<PatientAdminResponse> patients = patientAdminService.listPatients(pageable);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "PATIENTS_LISTED",
                        "Patients fetched successfully.",
                        patients,
                        Map.of(
                            "totalElements", patients.getTotalElements(),
                            "totalPages", patients.getTotalPages(),
                            "currentPage", patients.getNumber(),
                            "pageSize", patients.getSize()
                        ),
                        null
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.canView('patients')")
    public ResponseEntity<ApiResponse<PatientAdminResponse>> getPatient(@PathVariable Long id) {
        PatientAdminResponse response = patientAdminService.getPatient(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "PATIENT_RETRIEVED",
                        "Patient fetched successfully.",
                        response
                )
        );
    }

    @PostMapping
    @PreAuthorize("@permissionService.canCreate('patients')")
    public ResponseEntity<ApiResponse<PatientAdminResponse>> createPatient(@Valid @RequestBody PatientUpsertRequest request) {
        PatientAdminResponse response = patientAdminService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseFactory.success(
                        "PATIENT_CREATED",
                        "Patient created successfully.",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit('patients')")
    public ResponseEntity<ApiResponse<PatientAdminResponse>> updatePatient(@PathVariable Long id,
                                                                           @Valid @RequestBody PatientUpsertRequest request) {
        PatientAdminResponse response = patientAdminService.updatePatient(id, request);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "PATIENT_UPDATED",
                        "Patient updated successfully.",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.canDelete('patients')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientAdminService.deletePatient(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "PATIENT_DELETED",
                        "Patient deleted successfully."
                )
        );
    }
}
