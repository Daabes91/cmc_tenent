-- Add Arabic localization fields to products table
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS name_ar VARCHAR(255),
    ADD COLUMN IF NOT EXISTS description_ar TEXT,
    ADD COLUMN IF NOT EXISTS short_description_ar VARCHAR(500);
