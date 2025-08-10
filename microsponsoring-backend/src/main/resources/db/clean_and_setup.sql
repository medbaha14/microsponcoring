-- Clean and Setup Database Script
-- This script cleans the database and sets up fresh data with proper UUIDs

USE microsponsoring;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clear all tables to remove corrupted data
DELETE FROM password_reset_tokens;
DELETE FROM recognition_benefits;
DELETE FROM bank_accounts;
DELETE FROM invoices;
DELETE FROM sponsors;
DELETE FROM companies_non_profits;
DELETE FROM page_customizations;
DELETE FROM users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Insert a single admin user with proper UUID
INSERT INTO users (
    user_id, 
    email, 
    username, 
    password, 
    full_name, 
    active, 
    user_type, 
    status, 
    accepted_conditions, 
    bio, 
    location, 
    website_url, 
    is_verified, 
    created_at, 
    updated_at
) VALUES (
    UUID(), 
    'bousnina.baha14@gmail.com', 
    'admin', 
    '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 
    'Admin User', 
    true, 
    'ADMIN', 
    'ACTIVE', 
    true, 
    'System Administrator', 
    'Paris, France', 
    'https://microsponsoring.com', 
    true, 
    NOW(), 
    NOW()
);

-- Verify the admin user was created
SELECT 
    user_id, 
    email, 
    username, 
    user_type,
    created_at
FROM users 
WHERE username = 'admin';

-- Display success message
SELECT 'Database cleaned and admin user created successfully!' as status;
SELECT 'Username: admin' as username;
SELECT 'Password: password' as password; 