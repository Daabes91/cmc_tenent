-- Extend staff permissions to cover all navigation modules

-- New module columns (JSON arrays of actions: ["VIEW", "CREATE", "EDIT", "DELETE"])
ALTER TABLE staff_permissions
    ADD COLUMN IF NOT EXISTS calendar_permissions JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS materials_permissions JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS insurance_companies_permissions JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS billing_permissions JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS translations_permissions JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS clinic_settings_permissions JSONB DEFAULT '[]'::jsonb;

-- Backfill nulls to empty arrays for existing rows
UPDATE staff_permissions
SET
    calendar_permissions = COALESCE(calendar_permissions, '[]'::jsonb),
    materials_permissions = COALESCE(materials_permissions, '[]'::jsonb),
    insurance_companies_permissions = COALESCE(insurance_companies_permissions, '[]'::jsonb),
    billing_permissions = COALESCE(billing_permissions, '[]'::jsonb),
    translations_permissions = COALESCE(translations_permissions, '[]'::jsonb),
    clinic_settings_permissions = COALESCE(clinic_settings_permissions, '[]'::jsonb);

-- Grant full access for the seeded admin account to the new modules
UPDATE staff_permissions sp
SET
    calendar_permissions = '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    materials_permissions = '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    insurance_companies_permissions = '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    billing_permissions = '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    translations_permissions = '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    clinic_settings_permissions = '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb
FROM staff_users su
WHERE sp.staff_user_id = su.id
  AND su.email = 'admin@clinic.com';

-- Comments
COMMENT ON COLUMN staff_permissions.calendar_permissions IS 'JSON array of allowed actions for calendar module';
COMMENT ON COLUMN staff_permissions.materials_permissions IS 'JSON array of allowed actions for materials module';
COMMENT ON COLUMN staff_permissions.insurance_companies_permissions IS 'JSON array of allowed actions for insurance companies module';
COMMENT ON COLUMN staff_permissions.billing_permissions IS 'JSON array of allowed actions for billing module';
COMMENT ON COLUMN staff_permissions.translations_permissions IS 'JSON array of allowed actions for translations module';
COMMENT ON COLUMN staff_permissions.clinic_settings_permissions IS 'JSON array of allowed actions for clinic settings module';
