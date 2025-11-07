-- Add PayPal credential fields to clinic_settings so they can be managed from the admin panel
ALTER TABLE clinic_settings
ADD COLUMN IF NOT EXISTS paypal_client_id VARCHAR(200),
ADD COLUMN IF NOT EXISTS paypal_client_secret VARCHAR(500),
ADD COLUMN IF NOT EXISTS paypal_environment VARCHAR(20);

COMMENT ON COLUMN clinic_settings.paypal_client_id IS 'PayPal REST API client ID configured via admin settings';
COMMENT ON COLUMN clinic_settings.paypal_client_secret IS 'PayPal REST API client secret configured via admin settings';
COMMENT ON COLUMN clinic_settings.paypal_environment IS 'PayPal environment selection (sandbox or live)';
