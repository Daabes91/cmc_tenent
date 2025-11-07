CREATE TABLE IF NOT EXISTS staff_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    staff_user_id BIGINT NOT NULL REFERENCES staff_users(id) ON DELETE CASCADE,
    token VARCHAR(128) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

INSERT INTO staff_users (email, full_name, role, password_hash, status, created_at)
VALUES (
    'admin@clinic.com',
    'Clinic Admin',
    'ADMIN',
    '$2a$10$CwTycUXWue0Thq9StjUM0uJ8QoO5K3pZIq9UZEMHs4J5WNeiZ1QVm',
    'ACTIVE',
    NOW()
)
ON CONFLICT (email) DO NOTHING;
