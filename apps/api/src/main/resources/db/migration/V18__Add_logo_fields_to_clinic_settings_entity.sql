-- Add logo fields to clinic_settings table (core entity)
ALTER TABLE clinic_settings 
ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS logo_image_id VARCHAR(100);

-- Add comments for documentation
COMMENT ON COLUMN clinic_settings.logo_url IS 'URL of the clinic logo image from Cloudflare Images';
COMMENT ON COLUMN clinic_settings.logo_image_id IS 'Cloudflare Images ID for the clinic logo';