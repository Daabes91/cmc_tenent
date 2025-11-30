-- Add plan management fields to subscriptions table
-- This migration adds fields for plan tier tracking, payment method information,
-- cancellation management, and scheduled plan changes

-- Add plan tier field
ALTER TABLE subscriptions
ADD COLUMN plan_tier VARCHAR(50);

-- Add renewal date field
ALTER TABLE subscriptions
ADD COLUMN renewal_date TIMESTAMP;

-- Add payment method fields
ALTER TABLE subscriptions
ADD COLUMN payment_method_mask VARCHAR(100);

ALTER TABLE subscriptions
ADD COLUMN payment_method_type VARCHAR(50);

-- Add cancellation fields
ALTER TABLE subscriptions
ADD COLUMN cancellation_date TIMESTAMP;

ALTER TABLE subscriptions
ADD COLUMN cancellation_effective_date TIMESTAMP;

-- Add pending plan change fields (for scheduled downgrades)
ALTER TABLE subscriptions
ADD COLUMN pending_plan_tier VARCHAR(50);

ALTER TABLE subscriptions
ADD COLUMN pending_plan_effective_date TIMESTAMP;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_tier ON subscriptions(plan_tier);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_renewal_date ON subscriptions(renewal_date);
CREATE INDEX IF NOT EXISTS idx_subscriptions_pending_effective_date ON subscriptions(pending_plan_effective_date);

-- Add comments for documentation
COMMENT ON COLUMN subscriptions.plan_tier IS 'Current subscription plan tier (BASIC, PROFESSIONAL, ENTERPRISE, CUSTOM)';
COMMENT ON COLUMN subscriptions.renewal_date IS 'Next billing/renewal date for the subscription';
COMMENT ON COLUMN subscriptions.payment_method_mask IS 'Masked payment method display (e.g., Visa ****1234)';
COMMENT ON COLUMN subscriptions.payment_method_type IS 'Type of payment method (CREDIT_CARD, PAYPAL, etc.)';
COMMENT ON COLUMN subscriptions.cancellation_date IS 'Date when cancellation was requested';
COMMENT ON COLUMN subscriptions.cancellation_effective_date IS 'Date when cancellation takes effect';
COMMENT ON COLUMN subscriptions.pending_plan_tier IS 'Plan tier scheduled to take effect at next billing cycle';
COMMENT ON COLUMN subscriptions.pending_plan_effective_date IS 'Date when pending plan tier becomes active';
