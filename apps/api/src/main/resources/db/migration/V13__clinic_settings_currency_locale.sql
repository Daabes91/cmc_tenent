-- Add currency and locale configuration to clinic settings
ALTER TABLE clinic_settings
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'AED',
    ADD COLUMN locale VARCHAR(20) NOT NULL DEFAULT 'en-AE';
