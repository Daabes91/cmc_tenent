package com.clinic.modules.admin.dto;

public record DashboardSummaryResponse(
        long appointmentsToday,
        long pendingConfirmations,
        double revenueMonthToDate,
        long newPatients
) {
}
