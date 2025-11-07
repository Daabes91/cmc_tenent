-- Add Cloudflare Images configuration fields to clinic_settings
-- This allows admins to configure Cloudflare credentials via the admin panel
-- instead of requiring environment variables and redeployment

ALTER TABLE clinic_settings
ADD COLUMN IF NOT EXISTS cloudflare_account_id VARCHAR(100),
ADD COLUMN IF NOT EXISTS cloudflare_api_token VARCHAR(500);

COMMENT ON COLUMN clinic_settings.cloudflare_account_id IS 'Cloudflare Account ID for Images API';
COMMENT ON COLUMN clinic_settings.cloudflare_api_token IS 'Cloudflare API Token with Images permissions';
