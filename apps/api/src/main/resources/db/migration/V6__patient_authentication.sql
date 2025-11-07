-- Add password support for patient authentication

ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ;

CREATE UNIQUE INDEX IF NOT EXISTS uk_patients_email_lower
    ON patients (LOWER(email))
    WHERE email IS NOT NULL;
