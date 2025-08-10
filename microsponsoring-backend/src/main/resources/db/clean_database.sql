-- Clean Database Script
-- This script removes all data from the database to eliminate UTF-8 corruption

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clear all tables
DELETE FROM password_reset_tokens;
DELETE FROM recognition_benefits;
DELETE FROM bank_accounts;
DELETE FROM invoices;
DELETE FROM sponsors;
DELETE FROM companies_non_profits;
DELETE FROM page_customizations;
DELETE FROM users;

-- Reset auto-increment counters (if any)
ALTER TABLE password_reset_tokens AUTO_INCREMENT = 1;
ALTER TABLE recognition_benefits AUTO_INCREMENT = 1;
ALTER TABLE bank_accounts AUTO_INCREMENT = 1;
ALTER TABLE invoices AUTO_INCREMENT = 1;
ALTER TABLE sponsors AUTO_INCREMENT = 1;
ALTER TABLE companies_non_profits AUTO_INCREMENT = 1;
ALTER TABLE page_customizations AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Verify tables are empty
SELECT 'Database cleaned successfully!' as status;
SELECT COUNT(*) as users_count FROM users;
SELECT COUNT(*) as organizations_count FROM companies_non_profits;
SELECT COUNT(*) as sponsors_count FROM sponsors;
SELECT COUNT(*) as recognition_benefits_count FROM recognition_benefits; 