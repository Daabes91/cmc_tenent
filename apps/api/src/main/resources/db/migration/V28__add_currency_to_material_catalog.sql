-- Add currency field to material_catalog to track original currency of prices

ALTER TABLE material_catalog ADD COLUMN currency VARCHAR(3);

-- Set default currency for existing materials to USD
-- Admin can update based on their actual currency when they were entered
UPDATE material_catalog SET currency = 'USD' WHERE currency IS NULL;

-- Make currency NOT NULL after setting defaults
ALTER TABLE material_catalog ALTER COLUMN currency SET NOT NULL;

-- Add index for currency-based queries
CREATE INDEX idx_material_catalog_currency ON material_catalog(currency);

COMMENT ON COLUMN material_catalog.currency IS 'Original currency of the unit_cost price (e.g., USD, JOD, AED)';
