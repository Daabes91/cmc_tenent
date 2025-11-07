-- Add currency field to payments table for multi-currency support
-- This ensures treatment plan payments also track their currency

ALTER TABLE payments ADD COLUMN currency VARCHAR(3);

-- Set default currency for existing payments to USD
-- Adjust this to your clinic's default currency if needed
UPDATE payments SET currency = 'USD' WHERE currency IS NULL;

-- Make currency NOT NULL after setting defaults
ALTER TABLE payments ALTER COLUMN currency SET NOT NULL;

-- Add index for currency-based queries
CREATE INDEX idx_payments_currency ON payments(currency);
