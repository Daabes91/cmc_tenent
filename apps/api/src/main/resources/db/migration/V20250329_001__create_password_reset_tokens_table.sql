-- Create password_reset_tokens table for forgot password functionality

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    staff_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_password_reset_tokens_staff
        FOREIGN KEY (staff_id) REFERENCES staff_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_password_reset_tokens_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

-- Create indexes for efficient queries
CREATE INDEX idx_password_reset_tokens_staff_id ON password_reset_tokens(staff_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_used ON password_reset_tokens(used);
CREATE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens(token_hash);
