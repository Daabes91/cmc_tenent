CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    custom_domain VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO tenants (slug, name, status)
VALUES ('default', 'Default Clinic', 'ACTIVE')
ON CONFLICT (slug) DO NOTHING;

ALTER TABLE clinic_settings
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

UPDATE clinic_settings
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

ALTER TABLE clinic_settings
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE clinic_settings
    ADD CONSTRAINT fk_clinic_settings_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_clinic_settings_tenant
    ON clinic_settings (tenant_id);
