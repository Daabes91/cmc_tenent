-- ============================================
-- Create SAAS Manager Script
-- ============================================
-- This script creates a SAAS Manager account
-- Run this with: psql -h localhost -p 5442 -U clinic -d clinic_multi_tenant -f create-saas-manager.sql
-- Or: PGPASSWORD=clinic_password psql -h localhost -p 5442 -U clinic -d clinic_multi_tenant -f create-saas-manager.sql

-- Delete any existing SAAS Manager with this email
DELETE FROM saas_managers WHERE email = 'admin@saas.com';

-- ============================================
-- Option 1: Password = "Admin123!"
-- ============================================
INSERT INTO saas_managers (email, full_name, password_hash, status, created_at, updated_at)
VALUES (
    'admin@saas.com',
    'SAAS Administrator',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ACTIVE',
    NOW(),
    NOW()
);

-- ============================================
-- Verify the SAAS Manager was created
-- ============================================
SELECT 
    id, 
    email, 
    full_name, 
    status, 
    created_at,
    'Password: Admin123!' as credentials
FROM saas_managers 
WHERE email = 'admin@saas.com';

-- ============================================
-- ALTERNATIVE OPTIONS (comment out the INSERT above and uncomment one below)
-- ============================================

-- Option 2: Password = "password"
-- DELETE FROM saas_managers WHERE email = 'admin@saas.com';
-- INSERT INTO saas_managers (email, full_name, password_hash, status, created_at, updated_at)
-- VALUES (
--     'admin@saas.com',
--     'SAAS Administrator',
--     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
--     'ACTIVE',
--     NOW(),
--     NOW()
-- );

-- Option 3: Password = "admin123"
-- DELETE FROM saas_managers WHERE email = 'admin@saas.com';
-- INSERT INTO saas_managers (email, full_name, password_hash, status, created_at, updated_at)
-- VALUES (
--     'admin@saas.com',
--     'SAAS Administrator',
--     '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzQzQzQzQz',
--     'ACTIVE',
--     NOW(),
--     NOW()
-- );

-- ============================================
-- CREATE YOUR OWN
-- ============================================
-- To create with a custom password:
-- 1. Generate a BCrypt hash online at: https://bcrypt-generator.com/
-- 2. Use rounds = 10
-- 3. Replace the password_hash value below with your generated hash
-- 4. Uncomment and run:

-- DELETE FROM saas_managers WHERE email = 'your-email@example.com';
-- INSERT INTO saas_managers (email, full_name, password_hash, status, created_at, updated_at)
-- VALUES (
--     'your-email@example.com',
--     'Your Full Name',
--     'YOUR_BCRYPT_HASH_HERE',
--     'ACTIVE',
--     NOW(),
--     NOW()
-- );

-- ============================================
-- TEST THE LOGIN
-- ============================================
-- After running this script, test the login with:
-- curl -X POST http://localhost:8080/saas/auth/login \
--   -H "Content-Type: application/json" \
--   -d '{"email":"admin@saas.com","password":"Admin123!"}'
