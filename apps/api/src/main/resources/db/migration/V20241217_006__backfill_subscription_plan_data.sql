-- Data migration to backfill plan tier and related fields for existing subscriptions
-- This migration populates the new plan management fields added in V20241217_005

-- Step 1: Backfill plan_tier based on tenant billing_status
-- Map billing status to appropriate plan tiers
UPDATE subscriptions s
SET plan_tier = CASE 
    WHEN t.billing_status = 'ACTIVE' THEN 'PROFESSIONAL'
    WHEN t.billing_status = 'PAST_DUE' THEN 'BASIC'
    WHEN t.billing_status = 'CANCELED' THEN 'BASIC'
    WHEN t.billing_status = 'PENDING_PAYMENT' THEN 'BASIC'
    ELSE 'BASIC'
END
FROM tenants t
WHERE s.tenant_id = t.id
AND s.plan_tier IS NULL;

-- Step 2: Calculate renewal_date based on subscription creation date and billing cycle
-- Assume monthly billing cycle (30 days) for existing subscriptions
-- If current_period_end exists, use it; otherwise calculate from created_at
UPDATE subscriptions
SET renewal_date = COALESCE(
    current_period_end,
    created_at + INTERVAL '30 days'
)
WHERE renewal_date IS NULL;

-- Step 3: Set default payment method information
-- For existing subscriptions without payment method details, set generic values
UPDATE subscriptions
SET 
    payment_method_type = 'PAYPAL',
    payment_method_mask = 'PayPal Account'
WHERE payment_method_type IS NULL
AND status IN ('ACTIVE', 'PAST_DUE');

-- Step 4: Handle canceled subscriptions
-- Set cancellation dates for subscriptions that are already canceled
UPDATE subscriptions s
SET 
    cancellation_date = s.updated_at,
    cancellation_effective_date = s.updated_at
WHERE s.status = 'CANCELED'
AND s.cancellation_date IS NULL;

-- Step 5: Ensure all active subscriptions have a plan tier
-- This is a safety check to ensure no active subscription is left without a tier
UPDATE subscriptions
SET plan_tier = 'BASIC'
WHERE plan_tier IS NULL
AND status IN ('ACTIVE', 'PAST_DUE', 'PENDING');

-- Add constraint to ensure plan_tier is not null for active subscriptions
-- Note: We don't add a NOT NULL constraint to allow flexibility for future statuses
-- but we ensure data integrity through application logic

-- Log migration completion
DO $$
DECLARE
    total_subscriptions INTEGER;
    backfilled_subscriptions INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_subscriptions FROM subscriptions;
    SELECT COUNT(*) INTO backfilled_subscriptions FROM subscriptions WHERE plan_tier IS NOT NULL;
    
    RAISE NOTICE 'Subscription data migration completed:';
    RAISE NOTICE '  Total subscriptions: %', total_subscriptions;
    RAISE NOTICE '  Subscriptions with plan tier: %', backfilled_subscriptions;
END $$;
