-- Create SAAS Managers table for platform-level administrators
-- These users manage tenants across the entire multi-tenant system

CREATE TABLE saas_managers (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for efficient queries
CREATE INDEX idx_saas_managers_email ON saas_managers(email);
CREATE INDEX idx_saas_managers_status ON saas_managers(status);

-- Add comment for documentation
COMMENT ON TABLE saas_managers IS 'Platform-level administrators who manage tenants across the multi-tenant system';
COMMENT ON COLUMN saas_managers.status IS 'Manager status: ACTIVE or SUSPENDED';
