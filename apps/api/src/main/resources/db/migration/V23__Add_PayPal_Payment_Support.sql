-- Add PayPal settings to clinic_settings table
-- Only business settings, credentials are in environment variables
ALTER TABLE clinic_settings 
ADD COLUMN virtual_consultation_fee DECIMAL(10,2);

-- Create paypal_payments table
CREATE TABLE paypal_payments (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    capture_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    slot_id VARCHAR(255),
    type VARCHAR(30) NOT NULL,
    payer_email VARCHAR(150),
    payer_name VARCHAR(200),
    raw_payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for paypal_payments table
CREATE INDEX idx_paypal_payment_order_id ON paypal_payments(order_id);
CREATE INDEX idx_paypal_payment_patient_id ON paypal_payments(patient_id);
CREATE INDEX idx_paypal_payment_status ON paypal_payments(status);

-- Add payment and source columns to appointments table
ALTER TABLE appointments 
ADD COLUMN paypal_payment_id BIGINT REFERENCES paypal_payments(id),
ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'WEB';

-- Update existing appointments to have WEB source
UPDATE appointments SET source = 'WEB' WHERE source IS NULL;

-- Add comment for clarity
COMMENT ON TABLE paypal_payments IS 'Stores PayPal payment records for virtual consultations';
COMMENT ON COLUMN paypal_payments.order_id IS 'PayPal order ID - unique identifier from PayPal';
COMMENT ON COLUMN paypal_payments.capture_id IS 'PayPal capture ID when payment is completed';
COMMENT ON COLUMN paypal_payments.status IS 'Payment status: PENDING, COMPLETED, FAILED, REFUNDED';
COMMENT ON COLUMN paypal_payments.type IS 'Payment type: VIRTUAL_CONSULTATION, CLINIC_VISIT';
COMMENT ON COLUMN appointments.paypal_payment_id IS 'Links appointment to PayPal payment record for virtual consultations';
COMMENT ON COLUMN appointments.source IS 'Booking source: WEB, ADMIN, PHONE';