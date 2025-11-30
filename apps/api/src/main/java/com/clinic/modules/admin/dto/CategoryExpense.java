package com.clinic.modules.admin.dto;

import java.math.BigDecimal;

public record CategoryExpense(
        String categoryName,
        BigDecimal amount
) {
}
