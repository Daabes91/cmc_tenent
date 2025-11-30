-- Add plan configuration support for multiple tiers/cycles
ALTER TABLE paypal_config
    ADD COLUMN IF NOT EXISTS plan_config TEXT;

-- Allow plan_id to be nullable (new multi-plan field is authoritative)
ALTER TABLE paypal_config
    ALTER COLUMN plan_id DROP NOT NULL;

-- Initialize plan_config with existing single plan_id where applicable
UPDATE paypal_config
SET plan_config = json_build_array(
        json_build_object(
            'tier', 'BASIC',
            'monthlyPlanId', NULLIF(plan_id, ''),
            'annualPlanId', NULL
        )
    )::text
WHERE (plan_config IS NULL OR trim(plan_config) = '')
  AND plan_id IS NOT NULL
  AND trim(plan_id) <> '';
