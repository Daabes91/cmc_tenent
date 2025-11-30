-- Add billing_status column to tenants table for PayPal subscription billing
-- Values: 'pending_payment', 'active', 'past_due', 'canceled'

ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS billing_status VARCHAR(50) NOT NULL DEFAULT 'pending_payment';

-- Create index for efficient billing status queries
CREATE INDEX IF NOT EXISTS idx_tenants_billing_status ON tenants(billing_status);

-- Update existing tenants to 'active' status (backward compatibility)
UPDATE tenants
SET billing_status = 'active'
WHERE billing_status = 'pending_payment';

-- Add comment for documentation
COMMENT ON COLUMN tenants.billing_status IS 'Billing status for PayPal subscription: pending_payment, active, past_due, canceled';
