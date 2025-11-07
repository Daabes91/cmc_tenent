-- Treatment Plans System
-- Manages patient-specific treatment plans with pricing, follow-ups, payments, and materials tracking

-- Material Catalog
CREATE TABLE material_catalog (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    unit_cost DECIMAL(10, 2) NOT NULL,
    unit_of_measure VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_material_catalog_active ON material_catalog(active);

-- Treatment Plans
CREATE TABLE treatment_plans (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE RESTRICT,
    treatment_type_id BIGINT NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    total_price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'JOD',
    planned_followups INTEGER NOT NULL,
    completed_visits INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    notes TEXT,
    discount_amount DECIMAL(10, 2),
    discount_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_treatment_plans_patient ON treatment_plans(patient_id);
CREATE INDEX idx_treatment_plans_doctor ON treatment_plans(doctor_id);
CREATE INDEX idx_treatment_plans_status ON treatment_plans(status);
CREATE INDEX idx_treatment_plans_treatment_type ON treatment_plans(treatment_type_id);

-- Follow-up Visits
CREATE TABLE followup_visits (
    id BIGSERIAL PRIMARY KEY,
    treatment_plan_id BIGINT NOT NULL REFERENCES treatment_plans(id) ON DELETE CASCADE,
    visit_number INTEGER NOT NULL,
    visit_date TIMESTAMP NOT NULL,
    notes TEXT,
    performed_procedures TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_followup_visits_plan ON followup_visits(treatment_plan_id);
CREATE INDEX idx_followup_visits_plan_number ON followup_visits(treatment_plan_id, visit_number);

-- Payments
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL REFERENCES followup_visits(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(32) NOT NULL,
    transaction_reference VARCHAR(255),
    payment_date TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_payments_visit ON payments(visit_id);
CREATE INDEX idx_payments_date ON payments(payment_date);

-- Material Usage
CREATE TABLE material_usage (
    id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL REFERENCES followup_visits(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES material_catalog(id) ON DELETE RESTRICT,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_cost DECIMAL(10, 2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_material_usage_visit ON material_usage(visit_id);
CREATE INDEX idx_material_usage_material ON material_usage(material_id);

-- Treatment Plan Audit Log
CREATE TABLE treatment_plan_audit_log (
    id BIGSERIAL PRIMARY KEY,
    treatment_plan_id BIGINT NOT NULL REFERENCES treatment_plans(id) ON DELETE CASCADE,
    changed_by_staff_id BIGINT,
    changed_by_name VARCHAR(255),
    change_type VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    notes TEXT,
    changed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_log_plan ON treatment_plan_audit_log(treatment_plan_id);
CREATE INDEX idx_audit_log_changed_at ON treatment_plan_audit_log(changed_at);

-- Insert some sample materials for testing
INSERT INTO material_catalog (name, description, unit_cost, unit_of_measure, active, created_at, updated_at) VALUES
('Dental Brackets (Metal)', 'Standard metal orthodontic brackets', 15.00, 'piece', TRUE, NOW(), NOW()),
('Orthodontic Wire', 'Nickel-titanium archwire', 25.00, 'piece', TRUE, NOW(), NOW()),
('Composite Resin', 'Tooth-colored filling material', 30.00, 'syringe', TRUE, NOW(), NOW()),
('Dental Cement', 'Permanent dental cement', 12.00, 'tube', TRUE, NOW(), NOW()),
('Anesthetic Cartridge', 'Local anesthetic (Lidocaine 2%)', 8.00, 'cartridge', TRUE, NOW(), NOW()),
('Dental Gloves (Box)', 'Nitrile examination gloves', 10.00, 'box', TRUE, NOW(), NOW()),
('Disposable Mask (Box)', 'Surgical face masks', 5.00, 'box', TRUE, NOW(), NOW());
