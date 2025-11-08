-- Add tenant scoping to patients table

-- Add tenant_id column
ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE patients
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE patients
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE patients
    ADD CONSTRAINT fk_patients_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Remove global email unique constraint if it exists
-- (The constraint was already dropped in V20241208_004__create_global_patients.sql)
-- This is here for completeness in case the migration order changes
DROP INDEX IF EXISTS uk_patients_email;

-- Create composite unique index on (tenant_id, global_patient_id)
-- This ensures a global patient can only have one profile per tenant
CREATE UNIQUE INDEX IF NOT EXISTS ux_patients_tenant_global_patient
    ON patients (tenant_id, global_patient_id);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_patients_tenant_id
    ON patients (tenant_id);

-- Create composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_patients_tenant_created
    ON patients (tenant_id, created_at DESC);
