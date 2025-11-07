-- Add virtual consultation meeting link to clinic settings
ALTER TABLE clinic_settings
    ADD COLUMN IF NOT EXISTS virtual_consultation_meeting_link VARCHAR(500);
