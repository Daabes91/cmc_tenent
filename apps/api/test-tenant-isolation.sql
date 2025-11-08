-- Test Tenant Isolation
-- This script manually tests tenant isolation by creating data in multiple tenants
-- and verifying that queries respect tenant boundaries

-- Clean up any existing test data
DELETE FROM doctors WHERE email = 'test-doctor@example.com';
DELETE FROM tenants WHERE slug IN ('test-tenant-a', 'test-tenant-b');

-- Create two test tenants
INSERT INTO tenants (slug, name, status, created_at, updated_at)
VALUES 
    ('test-tenant-a', 'Test Clinic A', 'ACTIVE', NOW(), NOW()),
    ('test-tenant-b', 'Test Clinic B', 'ACTIVE', NOW(), NOW())
ON CONFLICT (slug) DO UPDATE SET updated_at = NOW();

-- Get tenant IDs
DO $$
DECLARE
    tenant_a_id BIGINT;
    tenant_b_id BIGINT;
    doctor_a_id BIGINT;
    doctor_b_id BIGINT;
BEGIN
    -- Get tenant IDs
    SELECT id INTO tenant_a_id FROM tenants WHERE slug = 'test-tenant-a';
    SELECT id INTO tenant_b_id FROM tenants WHERE slug = 'test-tenant-b';
    
    RAISE NOTICE 'Tenant A ID: %', tenant_a_id;
    RAISE NOTICE 'Tenant B ID: %', tenant_b_id;
    
    -- Create doctor in tenant A
    INSERT INTO doctors (
        tenant_id, full_name_en, full_name_ar, specialty_en, specialty_ar,
        bio_en, bio_ar, locale, email, phone, display_order, is_active, created_at
    ) VALUES (
        tenant_a_id, 'Dr. John Smith', 'د. جون سميث', 'General Dentist', 'طبيب أسنان عام',
        'Experienced dentist', 'طبيب أسنان ذو خبرة', 'en,ar', 'test-doctor@example.com',
        '+1234567890', 0, true, NOW()
    ) RETURNING id INTO doctor_a_id;
    
    RAISE NOTICE 'Created Doctor A with ID: %', doctor_a_id;
    
    -- Create doctor in tenant B with SAME email (should be allowed)
    INSERT INTO doctors (
        tenant_id, full_name_en, full_name_ar, specialty_en, specialty_ar,
        bio_en, bio_ar, locale, email, phone, display_order, is_active, created_at
    ) VALUES (
        tenant_b_id, 'Dr. Jane Doe', 'د. جين دو', 'Orthodontist', 'أخصائي تقويم الأسنان',
        'Specialist in braces', 'متخصص في التقويم', 'en,ar', 'test-doctor@example.com',
        '+0987654321', 0, true, NOW()
    ) RETURNING id INTO doctor_b_id;
    
    RAISE NOTICE 'Created Doctor B with ID: %', doctor_b_id;
    
    -- Test 1: Query doctors for tenant A (should return only Dr. John Smith)
    RAISE NOTICE '=== Test 1: Query doctors for Tenant A ===';
    PERFORM full_name_en, email, tenant_id 
    FROM doctors 
    WHERE tenant_id = tenant_a_id AND is_active = true;
    
    IF (SELECT COUNT(*) FROM doctors WHERE tenant_id = tenant_a_id) = 1 THEN
        RAISE NOTICE '✓ PASS: Tenant A has exactly 1 doctor';
    ELSE
        RAISE NOTICE '✗ FAIL: Tenant A should have 1 doctor but has %', 
            (SELECT COUNT(*) FROM doctors WHERE tenant_id = tenant_a_id);
    END IF;
    
    -- Test 2: Query doctors for tenant B (should return only Dr. Jane Doe)
    RAISE NOTICE '=== Test 2: Query doctors for Tenant B ===';
    IF (SELECT COUNT(*) FROM doctors WHERE tenant_id = tenant_b_id) = 1 THEN
        RAISE NOTICE '✓ PASS: Tenant B has exactly 1 doctor';
    ELSE
        RAISE NOTICE '✗ FAIL: Tenant B should have 1 doctor but has %',
            (SELECT COUNT(*) FROM doctors WHERE tenant_id = tenant_b_id);
    END IF;
    
    -- Test 3: Verify same email exists in both tenants
    RAISE NOTICE '=== Test 3: Verify same email in different tenants ===';
    IF (SELECT COUNT(*) FROM doctors WHERE email = 'test-doctor@example.com') = 2 THEN
        RAISE NOTICE '✓ PASS: Same email exists in 2 different tenants';
    ELSE
        RAISE NOTICE '✗ FAIL: Expected 2 doctors with same email, found %',
            (SELECT COUNT(*) FROM doctors WHERE email = 'test-doctor@example.com');
    END IF;
    
    -- Test 4: Cross-tenant access (try to find doctor A using tenant B context)
    RAISE NOTICE '=== Test 4: Cross-tenant access prevention ===';
    IF NOT EXISTS (SELECT 1 FROM doctors WHERE id = doctor_a_id AND tenant_id = tenant_b_id) THEN
        RAISE NOTICE '✓ PASS: Cannot access Tenant A doctor from Tenant B context';
    ELSE
        RAISE NOTICE '✗ FAIL: Cross-tenant access should be prevented';
    END IF;
    
    -- Test 5: Verify doctor names are different
    RAISE NOTICE '=== Test 5: Verify doctors are different ===';
    IF (SELECT full_name_en FROM doctors WHERE id = doctor_a_id) = 'Dr. John Smith' AND
       (SELECT full_name_en FROM doctors WHERE id = doctor_b_id) = 'Dr. Jane Doe' THEN
        RAISE NOTICE '✓ PASS: Doctors have different names as expected';
    ELSE
        RAISE NOTICE '✗ FAIL: Doctor names do not match expected values';
    END IF;
    
    -- Display summary
    RAISE NOTICE '';
    RAISE NOTICE '=== Summary ===';
    RAISE NOTICE 'Tenant A (%) has % doctor(s)', tenant_a_id, 
        (SELECT COUNT(*) FROM doctors WHERE tenant_id = tenant_a_id);
    RAISE NOTICE 'Tenant B (%) has % doctor(s)', tenant_b_id,
        (SELECT COUNT(*) FROM doctors WHERE tenant_id = tenant_b_id);
    RAISE NOTICE 'Total doctors with email test-doctor@example.com: %',
        (SELECT COUNT(*) FROM doctors WHERE email = 'test-doctor@example.com');
    
    -- Show the doctors
    RAISE NOTICE '';
    RAISE NOTICE '=== Doctors in Tenant A ===';
    FOR doctor_a_id IN 
        SELECT id FROM doctors WHERE tenant_id = tenant_a_id
    LOOP
        RAISE NOTICE 'ID: %, Name: %, Email: %', 
            doctor_a_id,
            (SELECT full_name_en FROM doctors WHERE id = doctor_a_id),
            (SELECT email FROM doctors WHERE id = doctor_a_id);
    END LOOP;
    
    RAISE NOTICE '';
    RAISE NOTICE '=== Doctors in Tenant B ===';
    FOR doctor_b_id IN 
        SELECT id FROM doctors WHERE tenant_id = tenant_b_id
    LOOP
        RAISE NOTICE 'ID: %, Name: %, Email: %',
            doctor_b_id,
            (SELECT full_name_en FROM doctors WHERE id = doctor_b_id),
            (SELECT email FROM doctors WHERE id = doctor_b_id);
    END LOOP;
    
END $$;

-- Clean up test data
DELETE FROM doctors WHERE email = 'test-doctor@example.com';
DELETE FROM tenants WHERE slug IN ('test-tenant-a', 'test-tenant-b');

-- Test complete
SELECT '=== Test Complete - Test data cleaned up ===' AS status;
