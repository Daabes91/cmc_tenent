package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record PayPalSummaryDTO(
        BigDecimal totalRevenue,
        long totalTransactions,
        long completedTransactions,
        long pendingTransactions,
        long failedTransactions,
        BigDecimal averageTransactionValue,
        BigDecimal estimatedPayPalFees,
        BigDecimal netRevenue,
        double successRate,
        List<PayPalTransactionDTO> recentTransactions,
        PaginationInfo pagination
) {
    public record PaginationInfo(
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {}
}