package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportMetrics(
        long totalAppointments,
        long todayAppointments,
        long thisWeekAppointments,
        long thisMonthAppointments,
        long scheduledAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long totalPatients,
        long newPatientsThisMonth,
        long totalDoctors,
        long paymentsCollectedCount,
        long paymentsOutstandingCount,
        Map<String, Long> appointmentsByStatus,
        List<CategoryBreakdown> doctorBreakdown,
        List<CategoryBreakdown> serviceBreakdown,
        List<CategoryBreakdown> paymentMethodBreakdown,
        List<TimeSeriesPoint> appointmentTrend,
        List<RevenuePoint> revenueTrend,
        double averageDailyAppointments,
        double noShowRate,
        double collectionRate,
        long followUpVisitsThisMonth,
        long activeTreatmentPlans,
        BigDecimal revenueThisMonth,
        BigDecimal revenueLastMonth,
        // Multi-currency support
        Map<String, BigDecimal> revenueThisMonthByCurrency,
        Map<String, BigDecimal> revenueLastMonthByCurrency,
        Map<String, List<RevenuePoint>> revenueTrendByCurrency
) {
}
