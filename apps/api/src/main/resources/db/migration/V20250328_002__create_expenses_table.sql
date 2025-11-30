-- Create expenses table for finance module
CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    expense_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraint to tenants table
    CONSTRAINT fk_expenses_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Foreign key constraint to expense_categories table
    CONSTRAINT fk_expenses_category
        FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT,
    
    -- Check constraint: amount must be positive
    CONSTRAINT chk_expenses_amount_positive
        CHECK (amount > 0)
);

-- Index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_expenses_tenant 
    ON expenses(tenant_id);

-- Composite index for date range filtering by tenant
CREATE INDEX IF NOT EXISTS idx_expenses_tenant_date 
    ON expenses(tenant_id, expense_date DESC);

-- Composite index for category filtering by tenant
CREATE INDEX IF NOT EXISTS idx_expenses_tenant_category 
    ON expenses(tenant_id, category_id);

-- Add table comment
COMMENT ON TABLE expenses IS 'Clinic expense records for financial tracking. All expenses are tenant-isolated and associated with expense categories.';

-- Add column comments
COMMENT ON COLUMN expenses.amount IS 'Expense amount in the clinic currency. Must be positive (greater than zero).';
COMMENT ON COLUMN expenses.expense_date IS 'Date when the expense occurred. Used for date range filtering and monthly summaries.';
COMMENT ON COLUMN expenses.notes IS 'Optional notes or description for the expense.';
