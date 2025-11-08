ALTER TABLE staff_users
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

UPDATE staff_users
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

ALTER TABLE staff_users
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE staff_users
    ADD CONSTRAINT fk_staff_users_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE staff_users
    DROP CONSTRAINT IF EXISTS staff_users_email_key;

CREATE UNIQUE INDEX IF NOT EXISTS ux_staff_users_tenant_email
    ON staff_users (tenant_id, lower(email));
