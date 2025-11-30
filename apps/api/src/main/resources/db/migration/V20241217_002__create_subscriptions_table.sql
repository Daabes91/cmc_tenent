-- Create subscriptions table for PayPal subscription management
-- Tracks recurring billing subscriptions for each tenant

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'paypal',
    paypal_subscription_id VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    current_period_start TIMESTAMPTZ,
    current_period_end TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_subscriptions_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

-- Create indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_paypal_id ON subscriptions(paypal_subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);

-- Add comments for documentation
COMMENT ON TABLE subscriptions IS 'Stores PayPal subscription information for tenant billing';
COMMENT ON COLUMN subscriptions.provider IS 'Payment provider: paypal (future: stripe, etc.)';
COMMENT ON COLUMN subscriptions.status IS 'Subscription status: active, suspended, canceled, expired';
COMMENT ON COLUMN subscriptions.paypal_subscription_id IS 'PayPal subscription ID from PayPal API';
