package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.DashboardSummaryResponse;
import com.clinic.modules.admin.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardAdminController {

    private final DashboardService dashboardService;

    public DashboardAdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary() {
        DashboardSummaryResponse summary = dashboardService.getSummary(ZoneId.systemDefault());
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "DASHBOARD_SUMMARY",
                        "Dashboard summary fetched successfully.",
                        summary
                )
        );
    }
}
