-- Add payment detail fields to appointments for tracking collected amounts
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS payment_amount DECIMAL(10,2),
    ADD COLUMN IF NOT EXISTS payment_currency VARCHAR(3),
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(32),
    ADD COLUMN IF NOT EXISTS payment_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(120),
    ADD COLUMN IF NOT EXISTS payment_notes TEXT;
