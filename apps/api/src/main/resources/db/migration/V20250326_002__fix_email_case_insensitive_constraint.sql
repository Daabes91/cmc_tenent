-- Fix email uniqueness constraint to be case-insensitive
-- Drop the existing case-sensitive index and create a case-insensitive one

-- Drop the existing index if it exists
DROP INDEX IF EXISTS idx_patients_tenant_email;

-- Create a case-insensitive unique index using LOWER(email)
-- This ensures that emails like "test@example.com" and "TEST@EXAMPLE.COM" 
-- are treated as duplicates within the same tenant
CREATE UNIQUE INDEX idx_patients_tenant_email 
    ON patients(tenant_id, LOWER(email)) 
    WHERE email IS NOT NULL;

-- Add comment to explain the constraint
COMMENT ON INDEX idx_patients_tenant_email IS 'Ensures case-insensitive email uniqueness per tenant. The same email can be used across different tenants, but not within the same tenant.';
