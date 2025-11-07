-- Add image_url columns to doctors and services tables for Cloudflare Images integration

ALTER TABLE doctors
ADD COLUMN image_url VARCHAR(500);

ALTER TABLE services
ADD COLUMN image_url VARCHAR(500);

COMMENT ON COLUMN doctors.image_url IS 'URL to the doctor''s profile image stored in Cloudflare Images';
COMMENT ON COLUMN services.image_url IS 'URL to the service''s image stored in Cloudflare Images';
