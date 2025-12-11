-- Create clinic_settings table
CREATE TABLE clinic_settings (
    id BIGSERIAL PRIMARY KEY,
    clinic_name VARCHAR(200),
    phone VARCHAR(50),
    email VARCHAR(150),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),

    -- Working hours for each day
    monday_hours VARCHAR(50),
    tuesday_hours VARCHAR(50),
    wednesday_hours VARCHAR(50),
    thursday_hours VARCHAR(50),
    friday_hours VARCHAR(50),
    saturday_hours VARCHAR(50),
    sunday_hours VARCHAR(50),

    -- Social media links
    facebook_url VARCHAR(300),
    instagram_url VARCHAR(300),
    twitter_url VARCHAR(300),
    linkedin_url VARCHAR(300),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default clinic settings
INSERT INTO clinic_settings (
    clinic_name,
    phone,
    email,
    address,
    city,
    state,
    zip_code,
    country,
    monday_hours,
    tuesday_hours,
    wednesday_hours,
    thursday_hours,
    friday_hours,
    saturday_hours,
    sunday_hours
) VALUES (
    'Cliniqax''s Clinic',
    '+1 (555) 123-4567',
    'info@Cliniqaxsclinic.com',
    '123 Healthcare Avenue',
    'Medical City',
    'CA',
    '90001',
    'United States',
    '9:00 AM - 5:00 PM',
    '9:00 AM - 5:00 PM',
    '9:00 AM - 5:00 PM',
    '9:00 AM - 5:00 PM',
    '9:00 AM - 5:00 PM',
    '10:00 AM - 2:00 PM',
    'Closed'
);
