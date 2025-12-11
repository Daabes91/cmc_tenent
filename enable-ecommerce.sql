-- Enable ecommerce for a specific tenant
-- Replace 'your-tenant-slug' with your actual tenant slug

-- First, check current status
SELECT id, slug, name, ecommerce_enabled FROM tenants WHERE slug = 'your-tenant-slug';

-- Enable ecommerce for the tenant
UPDATE tenants SET ecommerce_enabled = true WHERE slug = 'your-tenant-slug';

-- Verify the change
SELECT id, slug, name, ecommerce_enabled FROM tenants WHERE slug = 'your-tenant-slug';

-- Alternative: Enable for all tenants (if you want to enable for all)
-- UPDATE tenants SET ecommerce_enabled = true;