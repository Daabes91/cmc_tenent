CREATE TABLE tenant_translations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    namespace VARCHAR(100) NOT NULL,
    translation_key VARCHAR(200) NOT NULL,
    locale VARCHAR(8) NOT NULL,
    value TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_tenant_translations_unique
    ON tenant_translations (tenant_id, namespace, translation_key, locale);
