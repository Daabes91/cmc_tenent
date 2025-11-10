-- Add hero media customization fields to clinic_settings table
ALTER TABLE clinic_settings 
ADD COLUMN hero_media_type VARCHAR(10) DEFAULT 'image',
ADD COLUMN hero_image_url TEXT,
ADD COLUMN hero_video_id VARCHAR(20);

-- Add comment for documentation
COMMENT ON COLUMN clinic_settings.hero_media_type IS 'Type of hero media: image or video';
COMMENT ON COLUMN clinic_settings.hero_image_url IS 'Full URL to the uploaded hero image from Cloudflare Images';
COMMENT ON COLUMN clinic_settings.hero_video_id IS 'YouTube video ID extracted from the URL';
