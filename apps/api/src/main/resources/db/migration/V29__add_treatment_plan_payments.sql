-- Create treatment_plan_payments table for direct payments against treatment plans
CREATE TABLE treatment_plan_payments (
    id BIGSERIAL PRIMARY KEY,
    treatment_plan_id BIGINT NOT NULL REFERENCES treatment_plans(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
    payment_method VARCHAR(32) NOT NULL,
    transaction_reference VARCHAR(255),
    payment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    notes TEXT,
    recorded_by_staff_id BIGINT NOT NULL,
    recorded_by_staff_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_treatment_plan_payments_treatment_plan_id ON treatment_plan_payments(treatment_plan_id);
CREATE INDEX idx_treatment_plan_payments_payment_date ON treatment_plan_payments(payment_date);
CREATE INDEX idx_treatment_plan_payments_recorded_by_staff_id ON treatment_plan_payments(recorded_by_staff_id);
CREATE INDEX idx_treatment_plan_payments_created_at ON treatment_plan_payments(created_at);

-- Add CHECK constraint for payment_method enum values
ALTER TABLE treatment_plan_payments 
ADD CONSTRAINT chk_payment_method 
CHECK (payment_method IN ('CASH', 'CARD', 'PAYPAL', 'POS', 'BANK_TRANSFER', 'CHECK', 'OTHER'));

-- Add comment to the table
COMMENT ON TABLE treatment_plan_payments IS 'Direct payments made against treatment plans, separate from visit-based payments';