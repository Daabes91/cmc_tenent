-- V15: Staff Permissions and Invitation System
--
-- This migration adds a granular permission system for staff members and
-- an invitation token system for password setup emails.

-- =============================================================================
-- STAFF PERMISSIONS TABLE
-- =============================================================================
-- Stores granular permissions for each staff member across different modules
-- Each permission is stored as a JSON array of action strings
CREATE TABLE IF NOT EXISTS staff_permissions (
    id BIGSERIAL PRIMARY KEY,
    staff_user_id BIGINT NOT NULL UNIQUE,

    -- Module permissions (JSON arrays of actions: ["VIEW", "CREATE", "EDIT", "DELETE"])
    appointments_permissions JSONB DEFAULT '[]'::jsonb,
    patients_permissions JSONB DEFAULT '[]'::jsonb,
    doctors_permissions JSONB DEFAULT '[]'::jsonb,
    services_permissions JSONB DEFAULT '[]'::jsonb,
    treatment_plans_permissions JSONB DEFAULT '[]'::jsonb,
    reports_permissions JSONB DEFAULT '[]'::jsonb,
    settings_permissions JSONB DEFAULT '[]'::jsonb,
    staff_permissions JSONB DEFAULT '[]'::jsonb,
    blogs_permissions JSONB DEFAULT '[]'::jsonb,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_staff_permissions_user
        FOREIGN KEY (staff_user_id)
        REFERENCES staff_users(id)
        ON DELETE CASCADE
);

-- Index for fast lookups by staff_user_id
CREATE INDEX idx_staff_permissions_user_id ON staff_permissions(staff_user_id);

-- =============================================================================
-- STAFF INVITATION TOKENS TABLE
-- =============================================================================
-- Stores one-time invitation tokens for new staff to set their password
CREATE TABLE IF NOT EXISTS staff_invitation_tokens (
    id BIGSERIAL PRIMARY KEY,
    staff_user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_staff_invitation_staff_user
        FOREIGN KEY (staff_user_id)
        REFERENCES staff_users(id)
        ON DELETE CASCADE
);

-- Index for fast token lookups
CREATE INDEX idx_staff_invitation_tokens_token ON staff_invitation_tokens(token);
CREATE INDEX idx_staff_invitation_tokens_user_id ON staff_invitation_tokens(staff_user_id);

-- =============================================================================
-- SEED DEFAULT PERMISSIONS FOR EXISTING ADMIN
-- =============================================================================
-- Grant full permissions to the existing admin@clinic.com user
INSERT INTO staff_permissions (
    staff_user_id,
    appointments_permissions,
    patients_permissions,
    doctors_permissions,
    services_permissions,
    treatment_plans_permissions,
    reports_permissions,
    settings_permissions,
    staff_permissions,
    blogs_permissions
)
SELECT
    id,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb,
    '["VIEW", "CREATE", "EDIT", "DELETE"]'::jsonb
FROM staff_users
WHERE email = 'admin@clinic.com'
ON CONFLICT (staff_user_id) DO NOTHING;

-- =============================================================================
-- COMMENTS
-- =============================================================================
COMMENT ON TABLE staff_permissions IS 'Stores granular permissions for staff members across all modules';
COMMENT ON COLUMN staff_permissions.appointments_permissions IS 'JSON array of allowed actions for appointments module';
COMMENT ON COLUMN staff_permissions.patients_permissions IS 'JSON array of allowed actions for patients module';
COMMENT ON COLUMN staff_permissions.doctors_permissions IS 'JSON array of allowed actions for doctors module';
COMMENT ON COLUMN staff_permissions.services_permissions IS 'JSON array of allowed actions for services module';
COMMENT ON COLUMN staff_permissions.treatment_plans_permissions IS 'JSON array of allowed actions for treatment plans module';
COMMENT ON COLUMN staff_permissions.reports_permissions IS 'JSON array of allowed actions for reports module';
COMMENT ON COLUMN staff_permissions.settings_permissions IS 'JSON array of allowed actions for settings module';
COMMENT ON COLUMN staff_permissions.staff_permissions IS 'JSON array of allowed actions for staff management module';
COMMENT ON COLUMN staff_permissions.blogs_permissions IS 'JSON array of allowed actions for blogs module';

COMMENT ON TABLE staff_invitation_tokens IS 'One-time invitation tokens for new staff to set their password';
COMMENT ON COLUMN staff_invitation_tokens.token IS 'Unique token sent via email for password setup';
COMMENT ON COLUMN staff_invitation_tokens.used_at IS 'Timestamp when the token was used (NULL if not yet used)';
