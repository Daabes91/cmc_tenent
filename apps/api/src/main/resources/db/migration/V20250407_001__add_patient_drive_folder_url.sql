-- Store an optional Google Drive folder link per patient for documents/images
ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS drive_folder_url VARCHAR(500);

COMMENT ON COLUMN patients.drive_folder_url IS 'Google Drive folder link for patient assets (images, history, visit files)';
