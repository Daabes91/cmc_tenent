package com.clinic.modules.core.finance;

import com.clinic.modules.core.tenant.TenantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a clinic expense for financial tracking.
 * All expenses are tenant-isolated and associated with expense categories.
 */
@Entity
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategoryEntity category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Default constructor for JPA.
     */
    protected ExpenseEntity() {
    }

    /**
     * Constructor for creating a new expense.
     *
     * @param category     the expense category
     * @param amount       the expense amount
     * @param expenseDate  the date of the expense
     * @param notes        optional notes about the expense
     */
    public ExpenseEntity(ExpenseCategoryEntity category, BigDecimal amount, LocalDate expenseDate, String notes) {
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.notes = notes;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public ExpenseCategoryEntity getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public void setCategory(ExpenseCategoryEntity category) {
        this.category = category;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Updates the expense details.
     *
     * @param category     the new expense category
     * @param amount       the new expense amount
     * @param expenseDate  the new expense date
     * @param notes        the new notes
     */
    public void updateDetails(ExpenseCategoryEntity category, BigDecimal amount, LocalDate expenseDate, String notes) {
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ExpenseEntity{" +
                "id=" + id +
                ", amount=" + amount +
                ", expenseDate=" + expenseDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
