ALTER TABLE services
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

UPDATE services
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

ALTER TABLE services
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE services
    ADD CONSTRAINT fk_services_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'services'::regclass
      AND contype = 'u'
      AND conkey = ARRAY[
          (SELECT attnum FROM pg_attribute WHERE attrelid = 'services'::regclass AND attname = 'slug')
      ];

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE services DROP CONSTRAINT %I', constraint_name);
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_services_tenant_slug
    ON services(tenant_id, slug);
