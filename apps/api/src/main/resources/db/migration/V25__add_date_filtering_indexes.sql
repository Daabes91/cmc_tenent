-- Migration to add database indexes for optimized date filtering in reports
-- This improves performance for date-range queries used in the reports functionality

-- Index on appointments.scheduled_at for date range queries
CREATE INDEX IF NOT EXISTS idx_appointments_scheduled_at 
ON appointments (scheduled_at) 
WHERE scheduled_at IS NOT NULL;

-- Index on appointments.created_at for fallback date queries
CREATE INDEX IF NOT EXISTS idx_appointments_created_at 
ON appointments (created_at);

-- Composite index for appointment status and date range queries
CREATE INDEX IF NOT EXISTS idx_appointments_status_scheduled_at 
ON appointments (status, scheduled_at) 
WHERE scheduled_at IS NOT NULL;

-- Composite index for appointment status and created_at (fallback)
CREATE INDEX IF NOT EXISTS idx_appointments_status_created_at 
ON appointments (status, created_at);

-- Index for payment collection status queries
CREATE INDEX IF NOT EXISTS idx_appointments_payment_collected_scheduled_at 
ON appointments (payment_collected, scheduled_at) 
WHERE scheduled_at IS NOT NULL;

-- Index for payment collection status with created_at fallback
CREATE INDEX IF NOT EXISTS idx_appointments_payment_collected_created_at 
ON appointments (payment_collected, created_at);

-- Index on patients.created_at for new patient counts
CREATE INDEX IF NOT EXISTS idx_patients_created_at 
ON patients (created_at);

-- Index on paypal_payments.created_at for PayPal transaction filtering
CREATE INDEX IF NOT EXISTS idx_paypal_payments_created_at 
ON paypal_payments (created_at);

-- Composite index for PayPal payment status and date queries
CREATE INDEX IF NOT EXISTS idx_paypal_payments_status_created_at 
ON paypal_payments (status, created_at);

-- Index on payments.payment_date for revenue calculations
CREATE INDEX IF NOT EXISTS idx_payments_payment_date 
ON payments (payment_date) 
WHERE payment_date IS NOT NULL;

-- Composite index for payment method and date queries
CREATE INDEX IF NOT EXISTS idx_payments_method_date 
ON payments (payment_method, payment_date) 
WHERE payment_date IS NOT NULL;

-- Index on followup_visits.visit_date for follow-up calculations
CREATE INDEX IF NOT EXISTS idx_followup_visits_visit_date 
ON followup_visits (visit_date) 
WHERE visit_date IS NOT NULL;

-- Index on treatment_plans.status for active plan counts
CREATE INDEX IF NOT EXISTS idx_treatment_plans_status 
ON treatment_plans (status);

-- Comments explaining the purpose of each index
COMMENT ON INDEX idx_appointments_scheduled_at IS 'Optimizes date range queries on appointment scheduled dates';
COMMENT ON INDEX idx_appointments_created_at IS 'Optimizes date range queries on appointment creation dates';
COMMENT ON INDEX idx_appointments_status_scheduled_at IS 'Optimizes status-filtered date range queries';
COMMENT ON INDEX idx_appointments_status_created_at IS 'Optimizes status-filtered date range queries (fallback)';
COMMENT ON INDEX idx_appointments_payment_collected_scheduled_at IS 'Optimizes payment status queries by date';
COMMENT ON INDEX idx_appointments_payment_collected_created_at IS 'Optimizes payment status queries by date (fallback)';
COMMENT ON INDEX idx_patients_created_at IS 'Optimizes new patient count queries';
COMMENT ON INDEX idx_paypal_payments_created_at IS 'Optimizes PayPal transaction date filtering';
COMMENT ON INDEX idx_paypal_payments_status_created_at IS 'Optimizes PayPal status and date queries';
COMMENT ON INDEX idx_payments_payment_date IS 'Optimizes revenue calculation queries';
COMMENT ON INDEX idx_payments_method_date IS 'Optimizes payment method breakdown queries';
COMMENT ON INDEX idx_followup_visits_visit_date IS 'Optimizes follow-up visit count queries';
COMMENT ON INDEX idx_treatment_plans_status IS 'Optimizes active treatment plan queries';