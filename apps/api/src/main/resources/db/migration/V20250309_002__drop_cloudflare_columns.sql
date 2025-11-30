-- Remove tenant-scoped Cloudflare credential columns (using global config instead)
ALTER TABLE clinic_settings
    DROP COLUMN IF EXISTS cloudflare_account_id,
    DROP COLUMN IF EXISTS cloudflare_api_token;

