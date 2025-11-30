-- Add Google OAuth support to global_patients table
ALTER TABLE global_patients
    ADD COLUMN IF NOT EXISTS google_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS google_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) DEFAULT 'LOCAL';

-- Make password_hash nullable for Google-only accounts
ALTER TABLE global_patients
    ALTER COLUMN password_hash DROP NOT NULL;

-- Add unique constraint for google_id
ALTER TABLE global_patients
    ADD CONSTRAINT uk_global_patients_google_id UNIQUE (google_id);

-- Add index for Google ID lookups
CREATE INDEX IF NOT EXISTS idx_global_patients_google_id ON global_patients(google_id);

-- Add google_id column to patients table (tenant-scoped)
ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);

-- Add tenant-scoped unique constraint for google_id
-- This ensures the same Google account can be used across different tenants
CREATE UNIQUE INDEX IF NOT EXISTS idx_patients_tenant_google 
    ON patients(tenant_id, google_id) 
    WHERE google_id IS NOT NULL;

-- Create oauth_states table for temporary state storage during OAuth flow
CREATE TABLE IF NOT EXISTS oauth_states (
    id BIGSERIAL PRIMARY KEY,
    state_token VARCHAR(255) NOT NULL UNIQUE,
    tenant_slug VARCHAR(100) NOT NULL,
    nonce VARCHAR(255) NOT NULL,
    redirect_uri TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    consumed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Add indexes for oauth_states table
CREATE INDEX IF NOT EXISTS idx_oauth_states_token ON oauth_states(state_token);
CREATE INDEX IF NOT EXISTS idx_oauth_states_expires ON oauth_states(expires_at);
CREATE INDEX IF NOT EXISTS idx_oauth_states_consumed ON oauth_states(consumed);

-- Add comment to explain the oauth_states table
COMMENT ON TABLE oauth_states IS 'Temporary storage for OAuth state parameters during Google OAuth flow. States expire after 5 minutes and are consumed after use.';
