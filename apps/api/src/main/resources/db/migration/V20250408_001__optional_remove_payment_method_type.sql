-- OPTIONAL MIGRATION: Remove payment_method_type field from subscriptions table
-- 
-- This migration is OPTIONAL and safe to run or skip. The system works correctly
-- with or without the payment_method_type field.
--
-- BACKGROUND:
-- The payment_method_type field was added to support both card-based and redirect-based
-- PayPal subscription flows. After rolling back to redirect-only flow, this field is
-- no longer needed for business logic.
--
-- SAFETY:
-- - This migration can be run at any time without affecting system functionality
-- - The system does not query or use payment_method_type in any business logic
-- - Existing subscriptions will continue to work normally
-- - If you choose not to run this migration, the field will simply remain unused
--
-- WHEN TO RUN:
-- - After confirming the redirect flow rollback is complete and stable
-- - During a maintenance window if you prefer extra caution
-- - Can be deferred indefinitely if you prefer to keep the field for historical data
--
-- TO SKIP THIS MIGRATION:
-- Simply do not apply this migration file. The system will function identically.

-- Drop the index first (if it exists)
DROP INDEX IF EXISTS idx_subscriptions_payment_method_type;

-- Drop the column (if it exists)
ALTER TABLE subscriptions
DROP COLUMN IF EXISTS payment_method_type;

-- Log the migration completion
DO $$
DECLARE
    column_exists BOOLEAN;
BEGIN
    -- Check if column still exists (it shouldn't after the DROP above)
    SELECT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'subscriptions' 
        AND column_name = 'payment_method_type'
    ) INTO column_exists;
    
    IF column_exists THEN
        RAISE NOTICE 'WARNING: payment_method_type column still exists after migration';
    ELSE
        RAISE NOTICE 'SUCCESS: payment_method_type column has been removed from subscriptions table';
        RAISE NOTICE 'The system will continue to function normally without this field';
    END IF;
END $$;

-- Add comment to document the removal
COMMENT ON TABLE subscriptions IS 'Stores PayPal subscription information for tenant billing. Note: payment_method_type field was removed as system now uses redirect-only flow.';
