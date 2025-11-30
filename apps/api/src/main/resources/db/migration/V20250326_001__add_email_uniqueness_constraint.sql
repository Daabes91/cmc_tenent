-- Add unique constraint for (tenant_id, email) in patients table
-- This ensures email uniqueness per tenant while allowing the same email across different tenants

-- First, check if there are any duplicate emails within the same tenant
-- This query will help identify any data issues before adding the constraint
DO $$
BEGIN
    -- Check for duplicates
    IF EXISTS (
        SELECT tenant_id, email, COUNT(*)
        FROM patients
        WHERE email IS NOT NULL
        GROUP BY tenant_id, email
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate emails found within the same tenant. Please resolve data issues before applying this migration.';
    END IF;
END $$;

-- Add unique constraint for (tenant_id, email)
-- This ensures that within a tenant, each email can only be used once
-- Using LOWER(email) to make the constraint case-insensitive
CREATE UNIQUE INDEX IF NOT EXISTS idx_patients_tenant_email 
    ON patients(tenant_id, LOWER(email)) 
    WHERE email IS NOT NULL;

-- Add comment to explain the constraint
COMMENT ON INDEX idx_patients_tenant_email IS 'Ensures case-insensitive email uniqueness per tenant. The same email can be used across different tenants, but not within the same tenant.';
