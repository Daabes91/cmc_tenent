-- Tenant Isolation Verification Script
-- This script tests that tenant isolation is working correctly

\echo '========================================='
\echo 'TENANT ISOLATION VERIFICATION TEST'
\echo '========================================='
\echo ''

-- Clean up any existing test data
DELETE FROM doctors WHERE email = 'isolation-test@example.com';
DELETE FROM tenants WHERE slug IN ('isolation-test-a', 'isolation-test-b');

\echo 'Step 1: Creating two test tenants...'
INSERT INTO tenants (slug, name, status, created_at, updated_at)
VALUES 
    ('isolation-test-a', 'Isolation Test Clinic A', 'ACTIVE', NOW(), NOW()),
    ('isolation-test-b', 'Isolation Test Clinic B', 'ACTIVE', NOW(), NOW());

\echo 'Step 2: Creating doctor in Tenant A with email isolation-test@example.com...'
INSERT INTO doctors (
    tenant_id, full_name_en, specialty_en, email, phone, 
    display_order, is_active, created_at
)
SELECT 
    t.id, 'Dr. Alice Anderson', 'General Dentist', 'isolation-test@example.com',
    '+1111111111', 0, true, NOW()
FROM tenants t WHERE t.slug = 'isolation-test-a';

\echo 'Step 3: Creating doctor in Tenant B with SAME email isolation-test@example.com...'
INSERT INTO doctors (
    tenant_id, full_name_en, specialty_en, email, phone,
    display_order, is_active, created_at
)
SELECT 
    t.id, 'Dr. Bob Brown', 'Orthodontist', 'isolation-test@example.com',
    '+2222222222', 0, true, NOW()
FROM tenants t WHERE t.slug = 'isolation-test-b';

\echo ''
\echo '========================================='
\echo 'TEST RESULTS'
\echo '========================================='
\echo ''

\echo 'Test 1: Verify both doctors were created'
SELECT 
    CASE 
        WHEN COUNT(*) = 2 THEN '✓ PASS: 2 doctors created with same email'
        ELSE '✗ FAIL: Expected 2 doctors, found ' || COUNT(*)
    END AS result
FROM doctors 
WHERE email = 'isolation-test@example.com';

\echo ''
\echo 'Test 2: Query doctors for Tenant A (should return only Dr. Alice Anderson)'
SELECT 
    d.id,
    d.full_name_en AS name,
    d.email,
    t.slug AS tenant
FROM doctors d
JOIN tenants t ON d.tenant_id = t.id
WHERE t.slug = 'isolation-test-a' 
  AND d.email = 'isolation-test@example.com';

SELECT 
    CASE 
        WHEN COUNT(*) = 1 THEN '✓ PASS: Tenant A query returns exactly 1 doctor'
        ELSE '✗ FAIL: Tenant A query returned ' || COUNT(*) || ' doctors'
    END AS result
FROM doctors d
JOIN tenants t ON d.tenant_id = t.id
WHERE t.slug = 'isolation-test-a' 
  AND d.email = 'isolation-test@example.com';

\echo ''
\echo 'Test 3: Query doctors for Tenant B (should return only Dr. Bob Brown)'
SELECT 
    d.id,
    d.full_name_en AS name,
    d.email,
    t.slug AS tenant
FROM doctors d
JOIN tenants t ON d.tenant_id = t.id
WHERE t.slug = 'isolation-test-b' 
  AND d.email = 'isolation-test@example.com';

SELECT 
    CASE 
        WHEN COUNT(*) = 1 THEN '✓ PASS: Tenant B query returns exactly 1 doctor'
        ELSE '✗ FAIL: Tenant B query returned ' || COUNT(*) || ' doctors'
    END AS result
FROM doctors d
JOIN tenants t ON d.tenant_id = t.id
WHERE t.slug = 'isolation-test-b' 
  AND d.email = 'isolation-test@example.com';

\echo ''
\echo 'Test 4: Cross-tenant access (try to find Tenant A doctor using Tenant B ID)'
WITH tenant_a_doctor AS (
    SELECT d.id AS doctor_id
    FROM doctors d
    JOIN tenants t ON d.tenant_id = t.id
    WHERE t.slug = 'isolation-test-a' 
      AND d.email = 'isolation-test@example.com'
    LIMIT 1
),
tenant_b AS (
    SELECT id AS tenant_id
    FROM tenants
    WHERE slug = 'isolation-test-b'
)
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ PASS: Cannot access Tenant A doctor from Tenant B context'
        ELSE '✗ FAIL: Cross-tenant access should be prevented'
    END AS result
FROM doctors d, tenant_a_doctor tad, tenant_b tb
WHERE d.id = tad.doctor_id 
  AND d.tenant_id = tb.tenant_id;

\echo ''
\echo 'Test 5: Verify doctors have different names'
WITH doctors_data AS (
    SELECT 
        d.full_name_en,
        t.slug
    FROM doctors d
    JOIN tenants t ON d.tenant_id = t.id
    WHERE d.email = 'isolation-test@example.com'
    ORDER BY t.slug
)
SELECT 
    CASE 
        WHEN COUNT(DISTINCT full_name_en) = 2 THEN '✓ PASS: Doctors have different names'
        ELSE '✗ FAIL: Doctors should have different names'
    END AS result
FROM doctors_data;

\echo ''
\echo '========================================='
\echo 'SUMMARY'
\echo '========================================='
\echo ''

SELECT 
    t.slug AS tenant,
    COUNT(d.id) AS doctor_count,
    STRING_AGG(d.full_name_en, ', ') AS doctor_names
FROM tenants t
LEFT JOIN doctors d ON d.tenant_id = t.id AND d.email = 'isolation-test@example.com'
WHERE t.slug IN ('isolation-test-a', 'isolation-test-b')
GROUP BY t.slug
ORDER BY t.slug;

\echo ''
\echo 'Cleaning up test data...'
DELETE FROM doctors WHERE email = 'isolation-test@example.com';
DELETE FROM tenants WHERE slug IN ('isolation-test-a', 'isolation-test-b');

\echo ''
\echo '========================================='
\echo 'TEST COMPLETE'
\echo '========================================='
