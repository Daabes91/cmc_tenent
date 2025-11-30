-- Create payment_transactions table for tracking individual payment events
-- Records each successful/failed payment for audit and reporting

CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    paypal_transaction_id VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_payment_transactions_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE
);

-- Create indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_payment_transactions_subscription_id ON payment_transactions(subscription_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_paypal_id ON payment_transactions(paypal_transaction_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_status ON payment_transactions(status);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_date ON payment_transactions(transaction_date);

-- Add comments for documentation
COMMENT ON TABLE payment_transactions IS 'Records individual payment transactions from PayPal webhooks';
COMMENT ON COLUMN payment_transactions.status IS 'Transaction status: completed, pending, failed, refunded';
COMMENT ON COLUMN payment_transactions.paypal_transaction_id IS 'PayPal transaction ID from webhook event';
COMMENT ON COLUMN payment_transactions.amount IS 'Payment amount in the specified currency';
COMMENT ON COLUMN payment_transactions.currency IS 'ISO 4217 currency code (e.g., USD, EUR)';
