-- Seed core clinic data for services, doctors, and sample appointments

INSERT INTO services (slug, name_en, name_ar, summary_en, summary_ar, created_at)
SELECT slug, name_en, name_ar, summary_en, summary_ar, NOW()
FROM (
    VALUES
        ('comprehensive-checkup', 'Comprehensive Dental Checkup', 'فحص شامل للأسنان',
         'Full oral health assessment with digital x-rays and prevention planning.', 'تقييم كامل لصحة الفم مع صور أشعة رقمية وخطة وقائية.'),
        ('teeth-whitening', 'Advanced Teeth Whitening', 'تبييض الأسنان المتقدم',
         'In-clinic whitening system delivering brighter smiles in one visit.', 'نتائج فورية مع أحدث تقنيات التبييض داخل العيادة.'),
        ('invisible-aligners', 'Invisible Aligners', 'تقويم شفاف',
         'Discreet orthodontic treatment tailored to your lifestyle.', 'حلول تقويمية غير مرئية تناسب نمط حياتك.')
) AS data(slug, name_en, name_ar, summary_en, summary_ar)
WHERE NOT EXISTS (SELECT 1 FROM services s WHERE s.slug = data.slug);

INSERT INTO doctors (full_name, specialty, bio, locale, created_at)
SELECT full_name, specialty, bio, locale, NOW()
FROM (
    VALUES
        ('Dr. Layla Rahman', 'Cosmetic Dentistry', 'Focuses on smile design and whitening treatments.', 'en,ar'),
        ('Dr. Omar Idris', 'Oral Surgery', 'Specialist in oral surgery and complex extractions.', 'en'),
        ('Dr. Noor Haddad', 'General Dentistry', 'Family dentist with focus on preventive care.', 'en,ar')
) AS data(full_name, specialty, bio, locale)
WHERE NOT EXISTS (SELECT 1 FROM doctors d WHERE d.full_name = data.full_name);

-- Map doctors to services
INSERT INTO doctor_services (doctor_id, service_id)
SELECT d.id, s.id
FROM doctors d
JOIN services s ON s.slug = 'teeth-whitening'
WHERE d.full_name = 'Dr. Layla Rahman'
  AND NOT EXISTS (
      SELECT 1 FROM doctor_services ds
      WHERE ds.doctor_id = d.id AND ds.service_id = s.id
  );

INSERT INTO doctor_services (doctor_id, service_id)
SELECT d.id, s.id
FROM doctors d
JOIN services s ON s.slug = 'invisible-aligners'
WHERE d.full_name = 'Dr. Layla Rahman'
  AND NOT EXISTS (
      SELECT 1 FROM doctor_services ds
      WHERE ds.doctor_id = d.id AND ds.service_id = s.id
  );

INSERT INTO doctor_services (doctor_id, service_id)
SELECT d.id, s.id
FROM doctors d
JOIN services s ON s.slug = 'comprehensive-checkup'
WHERE d.full_name = 'Dr. Noor Haddad'
  AND NOT EXISTS (
      SELECT 1 FROM doctor_services ds
      WHERE ds.doctor_id = d.id AND ds.service_id = s.id
  );

INSERT INTO doctor_services (doctor_id, service_id)
SELECT d.id, s.id
FROM doctors d
JOIN services s ON s.slug = 'comprehensive-checkup'
WHERE d.full_name = 'Dr. Omar Idris'
  AND NOT EXISTS (
      SELECT 1 FROM doctor_services ds
      WHERE ds.doctor_id = d.id AND ds.service_id = s.id
  );

-- Seed a sample patient
INSERT INTO patients (external_id, first_name, last_name, email, phone, created_at)
VALUES ('PAT-0001', 'Sara', 'Al-Hassan', 'sara.alhassan@example.com', '+971500000001', NOW())
ON CONFLICT (external_id) DO NOTHING;

-- Seed sample appointments if none exist
INSERT INTO appointments (patient_id, doctor_id, service_id, scheduled_at, status, notes, created_at)
SELECT p.id,
       d.id,
       s.id,
       NOW() + INTERVAL '2 hours',
       'SCHEDULED',
       'Initial Invisalign consultation',
       NOW()
FROM patients p
JOIN doctors d ON d.full_name = 'Dr. Layla Rahman'
JOIN services s ON s.slug = 'invisible-aligners'
WHERE p.external_id = 'PAT-0001'
  AND NOT EXISTS (
        SELECT 1 FROM appointments a
        WHERE a.patient_id = p.id
          AND a.doctor_id = d.id
          AND a.service_id = s.id
    );
