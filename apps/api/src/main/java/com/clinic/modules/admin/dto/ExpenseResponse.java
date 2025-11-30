package com.clinic.modules.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for expense data.
 * Includes all expense details with category information.
 */
public record ExpenseResponse(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        LocalDate expenseDate,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
