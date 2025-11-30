package com.clinic.modules.core.finance;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO for aggregated expense data by category.
 * Used for grouping expenses by category with total amounts.
 */
public class CategoryExpenseAggregation {

    private final String categoryName;
    private final BigDecimal totalAmount;

    /**
     * Constructor for JPA projection.
     *
     * @param categoryName the category name
     * @param totalAmount  the total amount for this category
     */
    public CategoryExpenseAggregation(String categoryName, BigDecimal totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryExpenseAggregation that)) return false;
        return Objects.equals(categoryName, that.categoryName) &&
               Objects.equals(totalAmount, that.totalAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, totalAmount);
    }

    @Override
    public String toString() {
        return "CategoryExpenseAggregation{" +
                "categoryName='" + categoryName + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
