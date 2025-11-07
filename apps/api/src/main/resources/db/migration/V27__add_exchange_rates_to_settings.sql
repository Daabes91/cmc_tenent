-- Add exchange rates to clinic_settings for currency conversion
-- Stores rates relative to USD as base currency

ALTER TABLE clinic_settings ADD COLUMN exchange_rates JSONB;

-- Set default exchange rates (approximate values, admin can update)
-- Format: { "USD": 1.0, "JOD": 0.71, "AED": 3.67, "SAR": 3.75, ... }
-- These are rates TO USD (multiply by this to get USD)
UPDATE clinic_settings
SET exchange_rates = '{
  "USD": 1.0,
  "JOD": 0.709,
  "AED": 0.272,
  "SAR": 0.267,
  "QAR": 0.275,
  "OMR": 2.597,
  "KWD": 3.261,
  "EUR": 1.085,
  "GBP": 1.267
}'::jsonb
WHERE exchange_rates IS NULL;

-- Create index for faster JSONB queries
CREATE INDEX idx_clinic_settings_exchange_rates ON clinic_settings USING gin (exchange_rates);

COMMENT ON COLUMN clinic_settings.exchange_rates IS 'Exchange rates to USD base currency. Example: {"JOD": 0.709} means 1 JOD = 0.709 USD';
