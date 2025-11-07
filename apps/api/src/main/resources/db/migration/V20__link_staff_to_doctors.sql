-- Add optional doctor_id foreign key to staff_users table
-- This allows staff members with role DOCTOR to be linked to their doctor profile

ALTER TABLE staff_users
ADD COLUMN doctor_id BIGINT;

-- Add foreign key constraint
ALTER TABLE staff_users
ADD CONSTRAINT fk_staff_users_doctor
FOREIGN KEY (doctor_id) REFERENCES doctors(id)
ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_staff_users_doctor_id ON staff_users(doctor_id);

-- Add unique constraint to ensure one staff account per doctor
ALTER TABLE staff_users
ADD CONSTRAINT uq_staff_users_doctor_id UNIQUE (doctor_id);

-- Optional: Add a check constraint to ensure only DOCTOR role staff can have doctor_id
ALTER TABLE staff_users
ADD CONSTRAINT chk_doctor_id_role
CHECK (
    (doctor_id IS NULL) OR
    (doctor_id IS NOT NULL AND role = 'DOCTOR')
);
