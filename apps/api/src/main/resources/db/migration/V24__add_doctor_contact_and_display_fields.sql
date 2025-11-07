-- Add contact and display fields to doctors table
ALTER TABLE doctors 
ADD COLUMN email VARCHAR(120),
ADD COLUMN phone VARCHAR(20),
ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0,
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;

-- Create index on display_order for sorting
CREATE INDEX idx_doctors_display_order ON doctors(display_order);

-- Create index on is_active for filtering
CREATE INDEX idx_doctors_is_active ON doctors(is_active);