-- Add Arabic localization columns to carousels and carousel items
ALTER TABLE carousels
ADD COLUMN IF NOT EXISTS name_ar VARCHAR(255);

ALTER TABLE carousel_items
ADD COLUMN IF NOT EXISTS title_ar VARCHAR(255),
ADD COLUMN IF NOT EXISTS subtitle_ar VARCHAR(255),
ADD COLUMN IF NOT EXISTS cta_text_ar VARCHAR(100);
