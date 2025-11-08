-- Add soft delete support to tenants table
-- Soft-deleted tenants are marked as deleted but retained for audit purposes

ALTER TABLE tenants ADD COLUMN deleted_at TIMESTAMP;

-- Index for efficient filtering of active vs deleted tenants
CREATE INDEX idx_tenants_deleted_at ON tenants(deleted_at);

-- Add comment for documentation
COMMENT ON COLUMN tenants.deleted_at IS 'Timestamp when tenant was soft-deleted. NULL indicates active tenant.';
