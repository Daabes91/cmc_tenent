-- Update payment_method_type field for card-based subscriptions
-- This migration sets default value to 'redirect' for existing subscriptions
-- and updates the comment to reflect the new values ('card' and 'redirect')

-- Set default value to 'redirect' for existing subscriptions that don't have a payment_method_type
UPDATE subscriptions
SET payment_method_type = 'redirect'
WHERE payment_method_type IS NULL;

-- Update comment to reflect the new payment method types
COMMENT ON COLUMN subscriptions.payment_method_type IS 'Type of payment method: "card" for card-based subscriptions, "redirect" for redirect-based subscriptions';

-- Add index for payment_method_type for efficient filtering
CREATE INDEX IF NOT EXISTS idx_subscriptions_payment_method_type ON subscriptions(payment_method_type);

-- Log migration completion
DO $$
DECLARE
    total_subscriptions INTEGER;
    redirect_subscriptions INTEGER;
    card_subscriptions INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_subscriptions FROM subscriptions;
    SELECT COUNT(*) INTO redirect_subscriptions FROM subscriptions WHERE payment_method_type = 'redirect';
    SELECT COUNT(*) INTO card_subscriptions FROM subscriptions WHERE payment_method_type = 'card';
    
    RAISE NOTICE 'Payment method type migration completed:';
    RAISE NOTICE '  Total subscriptions: %', total_subscriptions;
    RAISE NOTICE '  Redirect-based subscriptions: %', redirect_subscriptions;
    RAISE NOTICE '  Card-based subscriptions: %', card_subscriptions;
END $$;
