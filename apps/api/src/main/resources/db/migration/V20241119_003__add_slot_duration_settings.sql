-- Add configurable slot duration to clinic settings and appointments
ALTER TABLE clinic_settings
    ADD COLUMN IF NOT EXISTS slot_duration_minutes INTEGER NOT NULL DEFAULT 30;

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS slot_duration_minutes INTEGER NOT NULL DEFAULT 30;
