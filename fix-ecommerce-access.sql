-- Enable ecommerce for cliniqax tenant
-- Replace 'cliniqax' with your actual tenant slug if different

-- Check current status
SELECT id, slug, name, ecommerce_enabled FROM tenants WHERE slug LIKE '%cliniqax%' OR slug LIKE '%clinic%';

-- Enable ecommerce (replace 'your-tenant-slug' with the actual slug from above query)
UPDATE tenants SET ecommerce_enabled = true WHERE slug = 'your-tenant-slug';

-- Verify the change
SELECT id, slug, name, ecommerce_enabled FROM tenants WHERE slug = 'your-tenant-slug';