-- Align seeded admin password with application defaults
UPDATE staff_users
SET password_hash = '$2a$10$by1AXTqwnWkcegUJVhlFdu62ad4n71eqHE.3gvg3kLTKnxrc/VWk6'
WHERE email = 'admin@clinic.com';
