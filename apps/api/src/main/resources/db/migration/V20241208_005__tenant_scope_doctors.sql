-- Add tenant scoping to doctors table

-- Add tenant_id column
ALTER TABLE doctors
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE doctors
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE doctors
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE doctors
    ADD CONSTRAINT fk_doctors_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Create composite unique constraint for (tenant_id, email) if email is not null
-- This allows the same email to exist across different tenants
CREATE UNIQUE INDEX IF NOT EXISTS ux_doctors_tenant_email
    ON doctors (tenant_id, email)
    WHERE email IS NOT NULL;

-- Create composite index for tenant-scoped queries
CREATE INDEX IF NOT EXISTS idx_doctors_tenant_active
    ON doctors (tenant_id, is_active);

-- Add comment for documentation
COMMENT ON COLUMN doctors.tenant_id IS 'Foreign key to tenants table for multi-tenant isolation';
