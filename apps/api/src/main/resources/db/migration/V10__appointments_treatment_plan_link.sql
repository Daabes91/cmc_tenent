-- Link appointments to treatment plans for automatic follow-up scheduling
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS treatment_plan_id BIGINT REFERENCES treatment_plans(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS follow_up_visit_number INTEGER;

CREATE INDEX IF NOT EXISTS idx_appointments_treatment_plan
    ON appointments (treatment_plan_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_appointments_plan_visit_active
    ON appointments (treatment_plan_id, follow_up_visit_number)
    WHERE treatment_plan_id IS NOT NULL
      AND follow_up_visit_number IS NOT NULL
      AND status <> 'CANCELLED';
