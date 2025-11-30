package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long appointmentsToday,
        long pendingConfirmations,
        double revenueMonthToDate,
        long newPatients,
        // Expense data
        BigDecimal totalExpensesCurrentMonth,
        List<CategoryExpense> expensesByCategoryCurrentMonth,
        BigDecimal netResult
) {
}
