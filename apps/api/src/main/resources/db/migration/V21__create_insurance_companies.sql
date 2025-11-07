-- Create insurance companies table
CREATE TABLE insurance_companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    logo_url VARCHAR(500),
    website_url VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for active companies ordered by display_order
CREATE INDEX idx_insurance_companies_active_order ON insurance_companies (is_active, display_order);

-- Insert some sample data
INSERT INTO insurance_companies (name, logo_url, website_url, phone, email, description, is_active, display_order) VALUES
('Bupa Arabia', 'https://images.unsplash.com/photo-1560472354-b33ff0c44a43?q=80&w=400&auto=format&fit=crop', 'https://www.bupa.com.sa', '+966-11-123-4567', 'info@bupa.com.sa', 'Leading health insurance provider in Saudi Arabia', true, 1),
('Tawuniya', 'https://images.unsplash.com/photo-1551434678-e076c223a692?q=80&w=400&auto=format&fit=crop', 'https://www.tawuniya.com.sa', '+966-11-234-5678', 'info@tawuniya.com.sa', 'Comprehensive health insurance solutions', true, 2),
('Medgulf', 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?q=80&w=400&auto=format&fit=crop', 'https://www.medgulf.com.sa', '+966-11-345-6789', 'info@medgulf.com.sa', 'Trusted healthcare insurance partner', true, 3),
('Allianz Care', 'https://images.unsplash.com/photo-1554224155-6726b3ff858f?q=80&w=400&auto=format&fit=crop', 'https://www.allianzcare.com', '+966-11-456-7890', 'info@allianzcare.com', 'International health insurance coverage', true, 4);