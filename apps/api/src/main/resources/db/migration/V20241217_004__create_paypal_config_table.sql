-- Create paypal_config table for storing PayPal API credentials
-- Managed by SaaS administrators through the admin panel

CREATE TABLE IF NOT EXISTS paypal_config (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    client_secret_encrypted TEXT NOT NULL,
    plan_id VARCHAR(255) NOT NULL,
    webhook_id VARCHAR(255),
    sandbox_mode BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Add comments for documentation
COMMENT ON TABLE paypal_config IS 'Stores PayPal API configuration for subscription billing';
COMMENT ON COLUMN paypal_config.client_id IS 'PayPal REST API Client ID';
COMMENT ON COLUMN paypal_config.client_secret_encrypted IS 'Encrypted PayPal REST API Client Secret (AES-256)';
COMMENT ON COLUMN paypal_config.plan_id IS 'PayPal subscription plan ID for monthly billing';
COMMENT ON COLUMN paypal_config.webhook_id IS 'PayPal webhook ID for event notifications';
COMMENT ON COLUMN paypal_config.sandbox_mode IS 'Whether to use PayPal sandbox (true) or production (false)';
