package com.clinic.modules.admin.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating an existing expense.
 * All fields except notes are required.
 */
public record ExpenseUpdateRequest(
        
        @NotNull(message = "Category ID is required")
        Long categoryId,
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
        BigDecimal amount,
        
        @NotNull(message = "Expense date is required")
        LocalDate expenseDate,
        
        @Size(max = 5000, message = "Notes must not exceed 5000 characters")
        String notes
) {
}
