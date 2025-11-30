-- Billing audit log table for SaaS billing events
CREATE TABLE IF NOT EXISTS billing_audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    manager_id BIGINT,
    manager_email VARCHAR(255),
    manager_name VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    description TEXT,
    metadata JSONB,
    source VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_billing_audit_tenant ON billing_audit_log(tenant_id);
CREATE INDEX IF NOT EXISTS idx_billing_audit_action ON billing_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_billing_audit_created_at ON billing_audit_log(created_at DESC);
