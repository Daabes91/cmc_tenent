package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.DoctorAvailabilityRequest;
import com.clinic.modules.admin.dto.DoctorAvailabilityResponse;
import com.clinic.modules.admin.dto.DoctorAvailabilitySlotsRequest;
import com.clinic.modules.admin.service.DoctorAvailabilityAdminService;
import com.clinic.modules.publicapi.dto.AvailabilityRequest;
import com.clinic.modules.publicapi.dto.AvailabilitySlotResponse;
import com.clinic.modules.publicapi.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/doctors/{doctorId}/availability")
public class DoctorAvailabilityAdminController {

    private final DoctorAvailabilityAdminService service;
    private final AvailabilityService availabilityService;

    public DoctorAvailabilityAdminController(DoctorAvailabilityAdminService service,
                                             AvailabilityService availabilityService) {
        this.service = service;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorAvailabilityResponse>>> list(@PathVariable Long doctorId) {
        List<DoctorAvailabilityResponse> data = service.listAvailabilities(doctorId);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTOR_AVAILABILITY_LISTED",
                        "Doctor availability fetched successfully.",
                        data
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> create(@PathVariable Long doctorId,
                                                                          @Valid @RequestBody DoctorAvailabilityRequest request) {
        DoctorAvailabilityResponse response = service.createAvailability(doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseFactory.success(
                        "DOCTOR_AVAILABILITY_CREATED",
                        "Availability created successfully.",
                        response
                )
        );
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> update(@PathVariable Long doctorId,
                                                                          @PathVariable Long availabilityId,
                                                                          @Valid @RequestBody DoctorAvailabilityRequest request) {
        DoctorAvailabilityResponse response = service.updateAvailability(doctorId, availabilityId, request);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTOR_AVAILABILITY_UPDATED",
                        "Availability updated successfully.",
                        response
                )
        );
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long doctorId,
                                                    @PathVariable Long availabilityId) {
        service.deleteAvailability(doctorId, availabilityId);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTOR_AVAILABILITY_DELETED",
                        "Availability deleted successfully."
                )
        );
    }

    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> slots(@PathVariable Long doctorId,
                                                                             @Valid @RequestBody DoctorAvailabilitySlotsRequest request) {
        AvailabilityRequest availabilityRequest = new AvailabilityRequest(
                request.serviceSlug(),
                doctorId,
                request.date()
        );
        List<AvailabilitySlotResponse> slots = availabilityService.computeAvailability(availabilityRequest);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DOCTOR_AVAILABLE_SLOTS",
                        "Available slots fetched successfully.",
                        slots
                )
        );
    }
}
