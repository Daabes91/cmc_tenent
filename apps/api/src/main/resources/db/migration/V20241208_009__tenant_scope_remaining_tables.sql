-- Add tenant scoping to remaining tables: insurance_companies, material_catalog, treatment_plans, paypal_payments

-- ============================================
-- 1. INSURANCE_COMPANIES TABLE
-- ============================================

-- Add tenant_id column
ALTER TABLE insurance_companies
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE insurance_companies
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE insurance_companies
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE insurance_companies
    ADD CONSTRAINT fk_insurance_companies_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Update unique constraint for name to be tenant-scoped
-- Drop old unique constraint on name_en if it exists
ALTER TABLE insurance_companies
    DROP CONSTRAINT IF EXISTS insurance_companies_name_key;

-- Create composite unique constraint for (tenant_id, name_en)
-- This allows the same insurance company name to exist across different tenants
CREATE UNIQUE INDEX IF NOT EXISTS ux_insurance_companies_tenant_name_en
    ON insurance_companies (tenant_id, name_en);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_insurance_companies_tenant_id
    ON insurance_companies (tenant_id);

-- Create composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_insurance_companies_tenant_active_order
    ON insurance_companies (tenant_id, is_active, display_order);

-- Add comment for documentation
COMMENT ON COLUMN insurance_companies.tenant_id IS 'Foreign key to tenants table for multi-tenant isolation';


-- ============================================
-- 2. MATERIAL_CATALOG TABLE
-- ============================================

-- Add tenant_id column
ALTER TABLE material_catalog
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE material_catalog
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE material_catalog
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE material_catalog
    ADD CONSTRAINT fk_material_catalog_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Update unique constraint for name to be tenant-scoped
-- Drop old unique constraint on name
ALTER TABLE material_catalog
    DROP CONSTRAINT IF EXISTS material_catalog_name_key;

-- Create composite unique constraint for (tenant_id, name)
-- This allows the same material name to exist across different tenants
CREATE UNIQUE INDEX IF NOT EXISTS ux_material_catalog_tenant_name
    ON material_catalog (tenant_id, name);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_material_catalog_tenant_id
    ON material_catalog (tenant_id);

-- Create composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_material_catalog_tenant_active
    ON material_catalog (tenant_id, active);

-- Add comment for documentation
COMMENT ON COLUMN material_catalog.tenant_id IS 'Foreign key to tenants table for multi-tenant isolation';


-- ============================================
-- 3. TREATMENT_PLANS TABLE
-- ============================================

-- Add tenant_id column
ALTER TABLE treatment_plans
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE treatment_plans
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE treatment_plans
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE treatment_plans
    ADD CONSTRAINT fk_treatment_plans_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_treatment_plans_tenant_id
    ON treatment_plans (tenant_id);

-- Update existing indexes to include tenant_id for better query performance
-- Drop old indexes
DROP INDEX IF EXISTS idx_treatment_plans_patient;
DROP INDEX IF EXISTS idx_treatment_plans_doctor;
DROP INDEX IF EXISTS idx_treatment_plans_status;

-- Create new tenant-scoped indexes
CREATE INDEX IF NOT EXISTS idx_treatment_plans_tenant_patient
    ON treatment_plans (tenant_id, patient_id);

CREATE INDEX IF NOT EXISTS idx_treatment_plans_tenant_doctor
    ON treatment_plans (tenant_id, doctor_id);

CREATE INDEX IF NOT EXISTS idx_treatment_plans_tenant_status
    ON treatment_plans (tenant_id, status);

-- Add comment for documentation
COMMENT ON COLUMN treatment_plans.tenant_id IS 'Foreign key to tenants table for multi-tenant isolation';


-- ============================================
-- 4. PAYPAL_PAYMENTS TABLE
-- ============================================

-- Add tenant_id column
ALTER TABLE paypal_payments
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE paypal_payments
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE paypal_payments
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE paypal_payments
    ADD CONSTRAINT fk_paypal_payments_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Update unique constraint for order_id to be tenant-scoped
-- Drop old unique constraint on order_id
ALTER TABLE paypal_payments
    DROP CONSTRAINT IF EXISTS paypal_payments_order_id_key;

-- Create composite unique constraint for (tenant_id, order_id)
-- This allows the same order_id to exist across different tenants (unlikely but safe)
CREATE UNIQUE INDEX IF NOT EXISTS ux_paypal_payments_tenant_order_id
    ON paypal_payments (tenant_id, order_id);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_paypal_payments_tenant_id
    ON paypal_payments (tenant_id);

-- Update existing indexes to include tenant_id for better query performance
-- Drop old indexes
DROP INDEX IF EXISTS idx_paypal_payment_patient_id;
DROP INDEX IF EXISTS idx_paypal_payment_status;

-- Create new tenant-scoped indexes
CREATE INDEX IF NOT EXISTS idx_paypal_payments_tenant_patient
    ON paypal_payments (tenant_id, patient_id);

CREATE INDEX IF NOT EXISTS idx_paypal_payments_tenant_status
    ON paypal_payments (tenant_id, status);

-- Add comment for documentation
COMMENT ON COLUMN paypal_payments.tenant_id IS 'Foreign key to tenants table for multi-tenant isolation';
