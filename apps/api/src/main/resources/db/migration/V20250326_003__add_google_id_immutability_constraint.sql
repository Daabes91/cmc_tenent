-- Add database-level enforcement of Google ID immutability
-- This ensures that once a Google ID is set, it cannot be changed

-- Function to prevent Google ID changes in global_patients
CREATE OR REPLACE FUNCTION prevent_google_id_change_global_patients()
RETURNS TRIGGER AS $$
BEGIN
    -- If google_id is being changed (not null to different value)
    IF OLD.google_id IS NOT NULL AND NEW.google_id IS DISTINCT FROM OLD.google_id THEN
        RAISE EXCEPTION 'Google ID is immutable and cannot be changed once set (global_patients)';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to enforce Google ID immutability in global_patients
DROP TRIGGER IF EXISTS trigger_prevent_google_id_change_global_patients ON global_patients;
CREATE TRIGGER trigger_prevent_google_id_change_global_patients
    BEFORE UPDATE ON global_patients
    FOR EACH ROW
    EXECUTE FUNCTION prevent_google_id_change_global_patients();

-- Function to prevent Google ID changes in patients
CREATE OR REPLACE FUNCTION prevent_google_id_change_patients()
RETURNS TRIGGER AS $$
BEGIN
    -- If google_id is being changed (not null to different value)
    IF OLD.google_id IS NOT NULL AND NEW.google_id IS DISTINCT FROM OLD.google_id THEN
        RAISE EXCEPTION 'Google ID is immutable and cannot be changed once set (patients)';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to enforce Google ID immutability in patients
DROP TRIGGER IF EXISTS trigger_prevent_google_id_change_patients ON patients;
CREATE TRIGGER trigger_prevent_google_id_change_patients
    BEFORE UPDATE ON patients
    FOR EACH ROW
    EXECUTE FUNCTION prevent_google_id_change_patients();

-- Add comments to explain the triggers
COMMENT ON FUNCTION prevent_google_id_change_global_patients() IS 'Prevents modification of google_id once set in global_patients table for security and data integrity';
COMMENT ON FUNCTION prevent_google_id_change_patients() IS 'Prevents modification of google_id once set in patients table for security and data integrity';
