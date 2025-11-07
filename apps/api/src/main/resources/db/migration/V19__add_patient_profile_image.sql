-- Add profile_image_url column to patients table for Cloudflare Images integration

ALTER TABLE patients
ADD COLUMN profile_image_url VARCHAR(500);

COMMENT ON COLUMN patients.profile_image_url IS 'URL to the patient''s profile image stored in Cloudflare Images';
