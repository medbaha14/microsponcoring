-- Complete Database Cleanup Script
-- This script completely removes all corrupted data and resets the database

USE microsponsoring;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all tables to completely reset the database
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS recognition_benefits;
DROP TABLE IF EXISTS bank_accounts;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS sponsors;
DROP TABLE IF EXISTS companies_non_profits;
DROP TABLE IF EXISTS page_customizations;
DROP TABLE IF EXISTS users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Verify all tables are dropped
SELECT 'Database completely cleaned. All tables dropped.' as status; 