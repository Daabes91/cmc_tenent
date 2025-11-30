package com.clinic.modules.core.finance;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ExpenseEntity operations.
 * Provides methods for querying expenses with tenant isolation and filtering.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    /**
     * Find all expenses for a specific tenant, ordered by expense date descending.
     *
     * @param tenantId the tenant ID
     * @return list of expenses ordered by date (newest first)
     */
    @EntityGraph(attributePaths = {"category"})
    List<ExpenseEntity> findByTenantIdOrderByExpenseDateDesc(Long tenantId);

    /**
     * Find expenses for a specific tenant within a date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of expenses within the date range
     */
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT e FROM ExpenseEntity e WHERE e.tenant.id = :tenantId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<ExpenseEntity> findByTenantIdAndExpenseDateBetween(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find expenses for a specific tenant and category.
     *
     * @param tenantId   the tenant ID
     * @param categoryId the category ID
     * @return list of expenses for the specified category
     */
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT e FROM ExpenseEntity e WHERE e.tenant.id = :tenantId AND e.category.id = :categoryId ORDER BY e.expenseDate DESC")
    List<ExpenseEntity> findByTenantIdAndCategoryId(
            @Param("tenantId") Long tenantId,
            @Param("categoryId") Long categoryId
    );

    /**
     * Find an expense by ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param id       the expense ID
     * @return optional containing the expense if found
     */
    @EntityGraph(attributePaths = {"category"})
    Optional<ExpenseEntity> findByTenantIdAndId(Long tenantId, Long id);

    /**
     * Find expenses for a specific tenant, category, and date range.
     *
     * @param tenantId   the tenant ID
     * @param categoryId the category ID
     * @param startDate  the start date (inclusive)
     * @param endDate    the end date (inclusive)
     * @return list of expenses matching all criteria
     */
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT e FROM ExpenseEntity e WHERE e.tenant.id = :tenantId AND e.category.id = :categoryId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<ExpenseEntity> findByTenantIdAndCategoryIdAndExpenseDateBetween(
            @Param("tenantId") Long tenantId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Count expenses for a specific tenant.
     *
     * @param tenantId the tenant ID
     * @return count of expenses for the tenant
     */
    long countByTenantId(Long tenantId);

    /**
     * Count expenses for a specific tenant and category.
     *
     * @param tenantId   the tenant ID
     * @param categoryId the category ID
     * @return count of expenses for the tenant and category
     */
    @Query("SELECT COUNT(e) FROM ExpenseEntity e WHERE e.tenant.id = :tenantId AND e.category.id = :categoryId")
    long countByTenantIdAndCategoryId(@Param("tenantId") Long tenantId, @Param("categoryId") Long categoryId);

    /**
     * Calculate total expenses for a specific tenant within a date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return total expenses amount, or null if no expenses found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e WHERE e.tenant.id = :tenantId AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTenantIdAndExpenseDateBetween(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate expenses grouped by category for a specific tenant within a date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of category expense aggregations
     */
    @Query("SELECT new com.clinic.modules.core.finance.CategoryExpenseAggregation(e.category.name, SUM(e.amount)) " +
           "FROM ExpenseEntity e " +
           "WHERE e.tenant.id = :tenantId AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category.id, e.category.name " +
           "ORDER BY e.category.name")
    List<CategoryExpenseAggregation> sumAmountByTenantIdAndCategoryGroupedByCategory(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
