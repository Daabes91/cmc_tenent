-- Add timezone column to clinic_settings table
-- This allows timezone to be configured via admin UI instead of environment variable

ALTER TABLE clinic_settings
ADD COLUMN IF NOT EXISTS timezone VARCHAR(50);

-- Set default timezone to Asia/Amman if not already set
UPDATE clinic_settings
SET timezone = 'Asia/Amman'
WHERE timezone IS NULL;

-- Add comment to explain the column
COMMENT ON COLUMN clinic_settings.timezone IS 'IANA timezone identifier (e.g., Asia/Amman, Asia/Dubai). Used for all appointment scheduling and display.';
