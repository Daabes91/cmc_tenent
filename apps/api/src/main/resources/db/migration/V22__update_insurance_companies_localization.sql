-- Update insurance companies table to support localization
ALTER TABLE insurance_companies 
  RENAME COLUMN name TO name_en;

ALTER TABLE insurance_companies 
  RENAME COLUMN description TO description_en;

ALTER TABLE insurance_companies 
  ADD COLUMN name_ar VARCHAR(160),
  ADD COLUMN description_ar TEXT;

-- Update existing data to have English names in the new column structure
-- The data is already in the name_en column after the rename

-- Update sample data with Arabic translations
UPDATE insurance_companies SET 
  name_ar = 'بوبا العربية',
  description_ar = 'مقدم خدمات التأمين الصحي الرائد في المملكة العربية السعودية'
WHERE name_en = 'Bupa Arabia';

UPDATE insurance_companies SET 
  name_ar = 'التعاونية',
  description_ar = 'حلول التأمين الصحي الشاملة'
WHERE name_en = 'Tawuniya';

UPDATE insurance_companies SET 
  name_ar = 'مدجلف',
  description_ar = 'شريك التأمين الصحي الموثوق'
WHERE name_en = 'Medgulf';

UPDATE insurance_companies SET 
  name_ar = 'أليانز كير',
  description_ar = 'تغطية التأمين الصحي الدولية'
WHERE name_en = 'Allianz Care';