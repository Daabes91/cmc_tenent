-- Add bilingual support for doctor fields
-- This migration adds separate English and Arabic columns for full name, specialty, and bio

-- Add new bilingual columns
ALTER TABLE doctors 
ADD COLUMN full_name_en VARCHAR(160),
ADD COLUMN full_name_ar VARCHAR(160),
ADD COLUMN specialty_en VARCHAR(120),
ADD COLUMN specialty_ar VARCHAR(120),
ADD COLUMN bio_en TEXT,
ADD COLUMN bio_ar TEXT;

-- Migrate existing data to English columns
UPDATE doctors 
SET full_name_en = full_name,
    specialty_en = specialty,
    bio_en = bio;

-- Make full_name_en required (since it replaces the old full_name)
ALTER TABLE doctors 
ALTER COLUMN full_name_en SET NOT NULL;

-- Drop old columns
ALTER TABLE doctors 
DROP COLUMN full_name,
DROP COLUMN specialty,
DROP COLUMN bio;