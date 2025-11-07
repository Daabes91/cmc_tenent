-- Baseline schema for clinic platform (Phase 2)

CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(64) UNIQUE NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(160),
    phone VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    specialty VARCHAR(120),
    bio TEXT,
    locale VARCHAR(8),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(120) UNIQUE NOT NULL,
    name_en VARCHAR(160) NOT NULL,
    name_ar VARCHAR(160),
    summary_en TEXT,
    summary_ar TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS doctor_services (
    doctor_id BIGINT REFERENCES doctors(id) ON DELETE CASCADE,
    service_id BIGINT REFERENCES services(id) ON DELETE CASCADE,
    PRIMARY KEY (doctor_id, service_id)
);

CREATE TABLE IF NOT EXISTS staff_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(160) UNIQUE NOT NULL,
    full_name VARCHAR(160),
    role VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    two_factor_secret VARCHAR(64),
    status VARCHAR(16) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT REFERENCES patients(id),
    doctor_id BIGINT REFERENCES doctors(id),
    service_id BIGINT REFERENCES services(id),
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_appointments_doctor ON appointments (doctor_id, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments (patient_id, scheduled_at);
