package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Minimal calendar admin controller to satisfy existing admin-panel calls.
 * Returns an empty list for calendar periods until a full implementation is added.
 */
@RestController
@RequestMapping("/admin/calendar")
public class CalendarAdminController {

    @GetMapping("/periods")
    @PreAuthorize("@permissionService.canView('calendar')")
    public ResponseEntity<ApiResponse<List<Object>>> getPeriods() {
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "CALENDAR_PERIODS_EMPTY",
                        "No calendar periods are configured.",
                        List.of()
                )
        );
    }
}
