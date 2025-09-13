-- Setup Database for Microsponsoring Application
-- Run this script in MySQL to create the database

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS microsponsoring
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE microsponsoring;

-- Display success message
SELECT 'Database microsponsoring created successfully!' as status;
SELECT 'You can now run the Spring Boot application' as next_step;

