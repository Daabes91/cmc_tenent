-- Doctor availability windows to support scheduling and booking validation

CREATE TABLE IF NOT EXISTS doctor_availability (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    recurring_weekly BOOLEAN NOT NULL,
    day_of_week VARCHAR(16),
    specific_date DATE,
    start_time TIME WITHOUT TIME ZONE NOT NULL,
    end_time TIME WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    CONSTRAINT chk_doctor_availability_range CHECK (end_time > start_time),
    CONSTRAINT chk_doctor_availability_type CHECK (
        (recurring_weekly = TRUE AND day_of_week IS NOT NULL AND specific_date IS NULL)
        OR (recurring_weekly = FALSE AND day_of_week IS NULL AND specific_date IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_doctor_availability_doctor ON doctor_availability (doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_availability_weekly ON doctor_availability (doctor_id, day_of_week) WHERE recurring_weekly = TRUE;
CREATE INDEX IF NOT EXISTS idx_doctor_availability_specific ON doctor_availability (doctor_id, specific_date) WHERE recurring_weekly = FALSE;

