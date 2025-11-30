-- Fix billing_status enum case to match Java enum constants
-- Convert lowercase values to uppercase to match BillingStatus enum

UPDATE tenants
SET billing_status = 'ACTIVE'
WHERE billing_status = 'active';

UPDATE tenants
SET billing_status = 'PENDING_PAYMENT'
WHERE billing_status = 'pending_payment';

UPDATE tenants
SET billing_status = 'PAST_DUE'
WHERE billing_status = 'past_due';

UPDATE tenants
SET billing_status = 'CANCELED'
WHERE billing_status = 'canceled';
