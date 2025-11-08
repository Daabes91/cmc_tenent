package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.DashboardSummaryResponse;
import com.clinic.modules.admin.dto.TeamOnCallResponse;
import com.clinic.modules.admin.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardAdminController {

    private final DashboardService dashboardService;

    public DashboardAdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
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

    @GetMapping("/team-on-call")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<TeamOnCallResponse>>> teamOnCall() {
        List<TeamOnCallResponse> team = dashboardService.getTeamOnCall(ZoneId.systemDefault());
        return ResponseEntity.ok(
                ApiResponseFactory.success(
                        "TEAM_ON_CALL",
                        "Team on call fetched successfully.",
                        team
                )
        );
    }
}
