package com.clinic.modules.admin.controller;

import com.clinic.api.ApiResponse;
import com.clinic.api.ApiResponseFactory;
import com.clinic.modules.admin.dto.PayPalSummaryDTO;
import com.clinic.modules.admin.dto.ReportMetrics;
import com.clinic.modules.admin.service.PayPalReportsService;
import com.clinic.modules.admin.service.ReportService;
import com.clinic.modules.admin.util.DateRange;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;
    private final PayPalReportsService payPalReportsService;

    public ReportController(ReportService reportService, PayPalReportsService payPalReportsService) {
        this.reportService = reportService;
        this.payPalReportsService = payPalReportsService;
    }

    @GetMapping("/metrics")
    @PreAuthorize("@permissionService.canView('reports')")
    public ResponseEntity<ApiResponse<ReportMetrics>> getMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            DateRange dateRange = DateRange.of(startDate, endDate);
            ReportMetrics metrics = reportService.getOverallMetrics(dateRange, ZoneId.systemDefault());
            return ResponseEntity.ok(
                    ApiResponseFactory.success(
                            "METRICS_RETRIEVED",
                            "Report metrics fetched successfully.",
                            metrics
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseFactory.<ReportMetrics>errorWithType(
                            "INVALID_DATE_RANGE",
                            e.getMessage(),
                            null
                    )
            );
        }
    }

    @GetMapping("/paypal-summary")
    @PreAuthorize("@permissionService.canView('reports')")
    public ResponseEntity<ApiResponse<PayPalSummaryDTO>> getPayPalSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            DateRange dateRange = DateRange.of(startDate, endDate);
            PayPalSummaryDTO summary = payPalReportsService.getPayPalSummary(page, size, dateRange);
            return ResponseEntity.ok(
                    ApiResponseFactory.success(
                            "PAYPAL_SUMMARY_RETRIEVED",
                            "PayPal summary fetched successfully.",
                            summary
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseFactory.<PayPalSummaryDTO>errorWithType(
                            "INVALID_DATE_RANGE",
                            e.getMessage(),
                            null
                    )
            );
        }
    }
}
