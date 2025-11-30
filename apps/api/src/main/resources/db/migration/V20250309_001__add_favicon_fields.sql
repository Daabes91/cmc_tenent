-- Add favicon fields for tenant branding
ALTER TABLE clinic_settings
    ADD COLUMN IF NOT EXISTS favicon_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS favicon_image_id VARCHAR(100);

