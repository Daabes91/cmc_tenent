-- For tenants where we previously defaulted email_enabled to false with no tenant-specific credentials,
-- clear the flag so the global email.enabled config can apply.
UPDATE clinic_settings
SET email_enabled = NULL
WHERE (email_enabled = FALSE OR email_enabled IS NULL)
  AND (sendgrid_api_key IS NULL OR sendgrid_api_key = '')
  AND (email_from IS NULL OR email_from = '')
  AND (email_from_name IS NULL OR email_from_name = '');
