-- Update profile_picture column size to accommodate longer file paths
ALTER TABLE users MODIFY COLUMN profile_picture VARCHAR(500);

-- Update image URL columns to accommodate longer file paths
ALTER TABLE page_customizations MODIFY COLUMN logo_url VARCHAR(500);
ALTER TABLE page_customizations MODIFY COLUMN banner_image_url VARCHAR(500);
ALTER TABLE page_customizations MODIFY COLUMN background_image_url VARCHAR(500); 