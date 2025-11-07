package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.AppointmentAdminDetailResponse;
import com.clinic.modules.admin.dto.AppointmentAdminUpsertRequest;
import com.clinic.modules.admin.dto.AppointmentDecisionRequest;
import com.clinic.modules.admin.dto.AppointmentResponse;
import com.clinic.modules.admin.service.AppointmentService;
import com.clinic.modules.admin.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/appointments")
public class AppointmentAdminController {

    private final AppointmentService appointmentService;
    private final NotificationService notificationService;

    public AppointmentAdminController(AppointmentService appointmentService,
                                     NotificationService notificationService) {
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.canView('appointments')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> listAppointments(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "doctorId", required = false) Long doctorId,
            @RequestParam(name = "patientId", required = false) Long patientId,
            @RequestParam(name = "serviceId", required = false) Long serviceId,
            @RequestParam(name = "bookingMode", required = false) String bookingMode,
            @RequestParam(name = "paymentCollected", required = false) Boolean paymentCollected,
            @RequestParam(name = "patientAttended", required = false) Boolean patientAttended,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "scheduledAt") String sortBy,
            @RequestParam(name = "direction", required = false, defaultValue = "DESC") String direction) {

        // Create pageable with sorting
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<AppointmentResponse> appointments = appointmentService.fetchAppointmentsWithAdvancedFilters(
            filter, status, doctorId, patientId, serviceId, bookingMode, 
            paymentCollected, patientAttended, fromDate, toDate, search,
            ZoneId.systemDefault(), pageable
        );
        
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "APPOINTMENTS_LISTED",
                        "Appointments fetched successfully.",
                        appointments,
                        Map.of(
                            "totalElements", appointments.getTotalElements(),
                            "totalPages", appointments.getTotalPages(),
                            "currentPage", appointments.getNumber(),
                            "pageSize", appointments.getSize(),
                            "appliedFilters", Map.of(
                                "filter", filter != null ? filter : "all",
                                "status", status != null ? status : "all",
                                "doctorId", doctorId != null ? doctorId : "all",
                                "patientId", patientId != null ? patientId : "all",
                                "serviceId", serviceId != null ? serviceId : "all",
                                "bookingMode", bookingMode != null ? bookingMode : "all",
                                "paymentCollected", paymentCollected != null ? paymentCollected : "all",
                                "patientAttended", patientAttended != null ? patientAttended : "all",
                                "dateRange", (fromDate != null || toDate != null) ? 
                                    Map.of("from", fromDate != null ? fromDate : "open", 
                                           "to", toDate != null ? toDate : "open") : "all",
                                "search", search != null ? search : "none"
                            )
                        ),
                        null
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.canView('appointments')")
    public ResponseEntity<ApiResponse<AppointmentAdminDetailResponse>> getAppointment(@PathVariable Long id) {
        AppointmentAdminDetailResponse response = appointmentService.getAppointment(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "APPOINTMENT_RETRIEVED",
                        "Appointment fetched successfully.",
                        response
                )
        );
    }

    @PostMapping
    @PreAuthorize("@permissionService.canCreate('appointments')")
    public ResponseEntity<ApiResponse<AppointmentAdminDetailResponse>> createAppointment(
            @Valid @RequestBody AppointmentAdminUpsertRequest request) {
        AppointmentAdminDetailResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseFactory.success(
                        "APPOINTMENT_CREATED",
                        "Appointment created successfully.",
                        response,
                        Map.of("status", response.status()),
                        Map.of("self", "/admin/appointments/" + response.id())
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.canEdit('appointments')")
    public ResponseEntity<ApiResponse<AppointmentAdminDetailResponse>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentAdminUpsertRequest request) {
        AppointmentAdminDetailResponse response = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "APPOINTMENT_UPDATED",
                        "Appointment updated successfully.",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.canDelete('appointments')")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "APPOINTMENT_DELETED",
                        "Appointment deleted successfully."
                )
        );
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@permissionService.canEdit('appointments')")
    public ResponseEntity<ApiResponse<AppointmentAdminDetailResponse>> approveAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) AppointmentDecisionRequest request) {
        AppointmentAdminDetailResponse response = appointmentService.approveAppointment(id, request != null ? request.notes() : null);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "APPOINTMENT_APPROVED",
                        "Appointment approved.",
                        response
                )
        );
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("@permissionService.canEdit('appointments')")
    public ResponseEntity<ApiResponse<AppointmentAdminDetailResponse>> declineAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) AppointmentDecisionRequest request) {
        AppointmentAdminDetailResponse response = appointmentService.declineAppointment(id, request != null ? request.notes() : null);
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "APPOINTMENT_DECLINED",
                        "Appointment declined.",
                        response
                )
        );
    }

    @GetMapping("/count/pending")
    @PreAuthorize("@permissionService.canView('appointments')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingCount() {
        long pendingCount = appointmentService.getPendingAppointmentsCount();
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "PENDING_COUNT_RETRIEVED",
                        "Pending appointments count fetched successfully.",
                        Map.of("count", pendingCount)
                )
        );
    }

    @GetMapping("/recent")
    @PreAuthorize("@permissionService.canView('appointments')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getRecentAppointments(
            @RequestParam(name = "since", required = false) String since,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        List<AppointmentResponse> appointments = appointmentService.getRecentAppointments(since, limit, ZoneId.systemDefault());
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "RECENT_APPOINTMENTS_RETRIEVED",
                        "Recent appointments fetched successfully.",
                        appointments,
                        Map.of("count", appointments.size()),
                        null
                )
        );
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("@permissionService.canView('appointments')")
    public SseEmitter streamNotifications(@RequestParam(name = "token", required = false) String token) {
        // Note: EventSource API doesn't support custom headers, so token is passed as query param
        // The token parameter is only used by StaffJwtAuthenticationFilter for authentication
        return notificationService.createEmitter();
    }
}
