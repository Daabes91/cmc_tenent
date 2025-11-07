-- Introduce booking mode for appointments (clinic vs virtual)

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS booking_mode VARCHAR(24) DEFAULT 'CLINIC_VISIT' NOT NULL;

CREATE INDEX IF NOT EXISTS idx_appointments_booking_mode
    ON appointments (booking_mode);
