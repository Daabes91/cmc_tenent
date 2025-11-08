-- Create global patients table for cross-tenant patient identity
CREATE TABLE IF NOT EXISTS global_patients (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(32),
    password_hash VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create index for email lookups
CREATE INDEX IF NOT EXISTS idx_global_patients_email ON global_patients(LOWER(email));

-- Migrate existing patients to global_patients table
-- Only migrate patients with both email and password_hash (authenticated patients)
INSERT INTO global_patients (external_id, email, phone, password_hash, date_of_birth, created_at, updated_at)
SELECT 
    external_id,
    email,
    phone,
    password_hash,
    date_of_birth,
    created_at,
    NOW()
FROM patients
WHERE email IS NOT NULL AND password_hash IS NOT NULL AND email != '';

-- Add global_patient_id column to patients table
ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS global_patient_id BIGINT;

-- Link existing patients to their global patient records
UPDATE patients p
SET global_patient_id = gp.id
FROM global_patients gp
WHERE p.external_id = gp.external_id;

-- Add foreign key constraint
ALTER TABLE patients
    ADD CONSTRAINT fk_patients_global_patient
        FOREIGN KEY (global_patient_id) REFERENCES global_patients(id);

-- Drop the unique constraint on patients.email since it will be global now
DROP INDEX IF EXISTS uk_patients_email_lower;
