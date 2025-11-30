-- Create expense_categories table for finance module
CREATE TABLE IF NOT EXISTS expense_categories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Foreign key constraint to tenants table
    CONSTRAINT fk_expense_categories_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Unique constraint: category name must be unique within a tenant
    CONSTRAINT uk_expense_categories_tenant_name
        UNIQUE (tenant_id, name)
);

-- Index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_expense_categories_tenant 
    ON expense_categories(tenant_id);

-- Composite index for filtering active categories by tenant
CREATE INDEX IF NOT EXISTS idx_expense_categories_tenant_active 
    ON expense_categories(tenant_id, is_active);

-- Add table comment
COMMENT ON TABLE expense_categories IS 'Expense categories for clinic financial tracking. Includes both system-seeded default categories and custom clinic-created categories.';

-- Add column comments
COMMENT ON COLUMN expense_categories.is_system IS 'TRUE for system-seeded default categories, FALSE for custom clinic-created categories. System categories cannot be deleted.';
COMMENT ON COLUMN expense_categories.is_active IS 'Controls whether the category appears in active category dropdowns. Inactive categories are hidden but historical expenses remain associated.';
