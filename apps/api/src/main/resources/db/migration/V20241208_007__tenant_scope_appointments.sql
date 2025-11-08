-- Add tenant scoping to appointments table

-- Add tenant_id column
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE appointments
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE appointments
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Create composite index on (tenant_id, scheduled_at) for efficient tenant-filtered queries
CREATE INDEX IF NOT EXISTS idx_appointments_tenant_scheduled
    ON appointments (tenant_id, scheduled_at);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_appointments_tenant_id
    ON appointments (tenant_id);

-- Update existing indexes to include tenant_id for better query performance
-- Drop old indexes
DROP INDEX IF EXISTS idx_appointments_doctor;
DROP INDEX IF EXISTS idx_appointments_patient;

-- Create new tenant-scoped indexes
CREATE INDEX IF NOT EXISTS idx_appointments_tenant_doctor_scheduled
    ON appointments (tenant_id, doctor_id, scheduled_at);

CREATE INDEX IF NOT EXISTS idx_appointments_tenant_patient_scheduled
    ON appointments (tenant_id, patient_id, scheduled_at);
