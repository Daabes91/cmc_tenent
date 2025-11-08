-- Add tenant scoping to blogs table

-- Add tenant_id column
ALTER TABLE blogs
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Backfill with default tenant
UPDATE blogs
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'default')
WHERE tenant_id IS NULL;

-- Make it required
ALTER TABLE blogs
    ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE blogs
    ADD CONSTRAINT fk_blogs_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Drop existing slug unique constraint
ALTER TABLE blogs
    DROP CONSTRAINT IF EXISTS blogs_slug_key;

-- Create composite unique index on (tenant_id, slug)
CREATE UNIQUE INDEX IF NOT EXISTS idx_blogs_tenant_slug
    ON blogs (tenant_id, slug);

-- Create index for tenant-based queries
CREATE INDEX IF NOT EXISTS idx_blogs_tenant_id
    ON blogs (tenant_id);

-- Update existing indexes to include tenant_id for better query performance
-- Drop old indexes that don't include tenant_id
DROP INDEX IF EXISTS idx_blogs_slug;
DROP INDEX IF EXISTS idx_blogs_status;
DROP INDEX IF EXISTS idx_blogs_status_locale;
DROP INDEX IF EXISTS idx_blogs_status_published_at;

-- Create new tenant-scoped indexes
CREATE INDEX IF NOT EXISTS idx_blogs_tenant_status
    ON blogs (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_blogs_tenant_status_locale
    ON blogs (tenant_id, status, locale);

CREATE INDEX IF NOT EXISTS idx_blogs_tenant_status_published_at
    ON blogs (tenant_id, status, published_at DESC);

CREATE INDEX IF NOT EXISTS idx_blogs_tenant_published_at
    ON blogs (tenant_id, published_at DESC);
