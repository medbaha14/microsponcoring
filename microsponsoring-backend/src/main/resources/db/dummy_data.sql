-- Dummy Data for Microsponsoring Application
-- This file contains sample data for testing and development
-- 30 users: 1 admin, 14 organizations, 15 sponsors with recognition benefits

-- Clear existing data to prevent conflicts
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

-- Insert Users (with explicit UUID generation and different creation dates)
INSERT INTO users (user_id, email, username, password, full_name, active, user_type, status, accepted_conditions, bio, location, website_url, is_verified, created_at, updated_at) VALUES
(NULL, 'admin@microsponsoring.com', 'admin', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Admin User', true, 'ADMIN', 'ACTIVE', true, 'System Administrator', 'Paris, France', 'https://microsponsoring.com', true, '2024-01-15 10:30:00', '2024-01-15 10:30:00'),
(NULL, 'redcross@example.com', 'redcross', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Red Cross France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Humanitarian aid organization helping people in crisis', 'Paris, France', 'https://www.croix-rouge.fr', true, '2024-02-01 09:00:00', '2024-02-01 09:00:00'),
(NULL, 'wwf@example.com', 'wwf', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'WWF France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Conservation organization working to preserve nature', 'Marseille, France', 'https://www.wwf.fr', true, '2024-02-05 11:30:00', '2024-02-05 11:30:00'),
(NULL, 'unicef@example.com', 'unicef', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'UNICEF France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'United Nations agency for children', 'Lyon, France', 'https://www.unicef.fr', true, '2024-02-10 16:45:00', '2024-02-10 16:45:00'),
(NULL, 'doctors@example.com', 'doctors', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Doctors Without Borders', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Medical humanitarian organization', 'Toulouse, France', 'https://www.msf.fr', true, '2024-02-15 13:20:00', '2024-02-15 13:20:00'),
(NULL, 'amnesty@example.com', 'amnesty', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Amnesty International France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Human rights organization defending justice and freedom', 'Nice, France', 'https://www.amnesty.fr', true, '2024-02-20 08:15:00', '2024-02-20 08:15:00'),
(NULL, 'greenpeace@example.com', 'greenpeace', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Greenpeace France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Environmental protection and peace organization', 'Bordeaux, France', 'https://www.greenpeace.fr', true, '2024-02-25 12:00:00', '2024-02-25 12:00:00'),
(NULL, 'handicap@example.com', 'handicap', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Handicap International', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Supporting people with disabilities worldwide', 'Lille, France', 'https://www.handicap-international.fr', true, '2024-03-01 15:30:00', '2024-03-01 15:30:00'),
(NULL, 'actionaid@example.com', 'actionaid', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Action Aid France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Fighting poverty and injustice globally', 'Strasbourg, France', 'https://www.actionaid.fr', true, '2024-03-05 10:45:00', '2024-03-05 10:45:00'),
(NULL, 'care@example.com', 'care', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'CARE France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'International humanitarian organization', 'Nantes, France', 'https://www.carefrance.org', true, '2024-03-10 14:20:00', '2024-03-10 14:20:00'),
(NULL, 'oxfam@example.com', 'oxfam', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Oxfam France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Fighting inequality and poverty', 'Montpellier, France', 'https://www.oxfamfrance.org', true, '2024-03-15 09:10:00', '2024-03-15 09:10:00'),
(NULL, 'savethechildren@example.com', 'savethechildren', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Save the Children France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Protecting children and their rights', 'Rennes, France', 'https://www.savethechildren.fr', true, '2024-03-20 11:55:00', '2024-03-20 11:55:00'),
(NULL, 'plan@example.com', 'plan', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Plan International France', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Advancing children rights and equality', 'Tours, France', 'https://www.plan-international.fr', true, '2024-03-25 16:40:00', '2024-03-25 16:40:00'),
(NULL, 'medecins@example.com', 'medecins', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Médecins du Monde', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Medical humanitarian organization', 'Grenoble, France', 'https://www.medecinsdumonde.org', true, '2024-03-30 13:25:00', '2024-03-30 13:25:00'),
(NULL, 'secours@example.com', 'secours', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Secours Populaire Français', true, 'ORGANISATION_NONPROFIT', 'ACTIVE', true, 'Solidarity and social justice organization', 'Dijon, France', 'https://www.secourspopulaire.fr', true, '2024-04-01 08:50:00', '2024-04-01 08:50:00'),
(NULL, 'john.doe@company.com', 'johndoe', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'John Doe', true, 'SPONSOR', 'ACTIVE', true, 'Tech entrepreneur passionate about social impact', 'Paris, France', 'https://johndoe.com', true, '2024-04-05 12:15:00', '2024-04-05 12:15:00'),
(NULL, 'marie.dupont@enterprise.com', 'mariedupont', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Marie Dupont', true, 'SPONSOR', 'ACTIVE', true, 'Business executive supporting environmental causes', 'Lyon, France', 'https://mariedupont.com', true, '2024-04-10 15:30:00', '2024-04-10 15:30:00'),
(NULL, 'pierre.martin@startup.com', 'pierremartin', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Pierre Martin', true, 'SPONSOR', 'ACTIVE', true, 'Startup founder focused on education initiatives', 'Marseille, France', 'https://pierremartin.com', true, '2024-04-15 10:45:00', '2024-04-15 10:45:00'),
(NULL, 'sophie.bernard@corp.com', 'sophiebernard', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Sophie Bernard', true, 'SPONSOR', 'ACTIVE', true, 'Corporate leader advocating for healthcare access', 'Toulouse, France', 'https://sophiebernard.com', true, '2024-04-20 14:20:00', '2024-04-20 14:20:00'),
(NULL, 'alexandre.leroy@tech.com', 'alexandreleroy', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Alexandre Leroy', true, 'SPONSOR', 'ACTIVE', true, 'Tech investor supporting innovation in social services', 'Nice, France', 'https://alexandreleroy.com', true, '2024-04-25 09:35:00', '2024-04-25 09:35:00'),
(NULL, 'emma.roussel@finance.com', 'emmaroussel', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Emma Roussel', true, 'SPONSOR', 'ACTIVE', true, 'Financial advisor committed to humanitarian causes', 'Bordeaux, France', 'https://emmaroussel.com', true, '2024-04-30 11:50:00', '2024-04-30 11:50:00'),
(NULL, 'thomas.moreau@consulting.com', 'thomasmoreau', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Thomas Moreau', true, 'SPONSOR', 'ACTIVE', true, 'Consulting firm owner supporting education', 'Lille, France', 'https://thomasmoreau.com', true, '2024-05-05 16:05:00', '2024-05-05 16:05:00'),
(NULL, 'lucie.petit@retail.com', 'luciepetit', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Lucie Petit', true, 'SPONSOR', 'ACTIVE', true, 'Retail business owner focused on environmental protection', 'Strasbourg, France', 'https://luciepetit.com', true, '2024-05-10 13:40:00', '2024-05-10 13:40:00'),
(NULL, 'antoine.lefevre@manufacturing.com', 'antoinelefevre', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Antoine Lefevre', true, 'SPONSOR', 'ACTIVE', true, 'Manufacturing executive supporting healthcare initiatives', 'Nantes, France', 'https://antoinelefevre.com', true, '2024-05-15 10:25:00', '2024-05-15 10:25:00'),
(NULL, 'camille.roy@pharma.com', 'camilleroy', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Camille Roy', true, 'SPONSOR', 'ACTIVE', true, 'Pharmaceutical executive advocating for medical research', 'Montpellier, France', 'https://camilleroy.com', true, '2024-05-20 15:15:00', '2024-05-20 15:15:00'),
(NULL, 'julien.blanc@energy.com', 'julienblanc', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Julien Blanc', true, 'SPONSOR', 'ACTIVE', true, 'Energy sector leader supporting renewable energy projects', 'Rennes, France', 'https://julienblanc.com', true, '2024-05-25 12:30:00', '2024-05-25 12:30:00'),
(NULL, 'nathalie.girard@education.com', 'nathaliegirard', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Nathalie Girard', true, 'SPONSOR', 'ACTIVE', true, 'Education consultant supporting literacy programs', 'Tours, France', 'https://nathaliegirard.com', true, '2024-05-30 09:45:00', '2024-05-30 09:45:00'),
(NULL, 'sebastien.mercier@realestate.com', 'sebastienmercier', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Sebastien Mercier', true, 'SPONSOR', 'ACTIVE', true, 'Real estate developer supporting housing initiatives', 'Grenoble, France', 'https://sebastienmercier.com', true, '2024-06-01 14:20:00', '2024-06-01 14:20:00'),
(NULL, 'isabelle.duval@hospitality.com', 'isabelleduval', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Isabelle Duval', true, 'SPONSOR', 'ACTIVE', true, 'Hospitality executive supporting community development', 'Dijon, France', 'https://isabelleduval.com', true, '2024-06-05 11:10:00', '2024-06-05 11:10:00'),
(NULL, 'francois.chevalier@law.com', 'francoischevalier', '$2a$12$Vo6BPULCU0JOH40Cfsl0t.4EvCY/MzRh9uP5DgApCpmVQ4dZgGzlm', 'Francois Chevalier', true, 'SPONSOR', 'ACTIVE', true, 'Law firm partner supporting human rights causes', 'Reims, France', 'https://francoischevalier.com', true, '2024-06-10 16:55:00', '2024-06-10 16:55:00');

-- Insert Companies/Non-Profits (14 organizations)
INSERT INTO companies_non_profits (company_id, user_id, activity_type, details, total_amount_received, total_sponsorships, created_at, updated_at) VALUES
(UUID(), (SELECT user_id FROM users WHERE email = 'redcross@example.com' LIMIT 1), 'HUMANITARIAN', 'Emergency response, disaster relief, blood donation services, first aid training', 15000.00, 8, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'wwf@example.com' LIMIT 1), 'ENVIRONMENTAL', 'Wildlife conservation, climate change initiatives, sustainable development projects', 22000.00, 12, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'unicef@example.com' LIMIT 1), 'CHILDREN', 'Child protection, education programs, health services for children', 18000.00, 10, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'doctors@example.com' LIMIT 1), 'HEALTHCARE', 'Medical assistance, emergency healthcare, vaccination campaigns', 25000.00, 15, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'amnesty@example.com' LIMIT 1), 'HUMAN_RIGHTS', 'Human rights advocacy, legal support, awareness campaigns', 12000.00, 6, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'greenpeace@example.com' LIMIT 1), 'ENVIRONMENTAL', 'Environmental protection, anti-pollution campaigns, renewable energy advocacy', 19000.00, 9, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'handicap@example.com' LIMIT 1), 'DISABILITY', 'Disability support, accessibility programs, inclusive development', 14000.00, 7, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'actionaid@example.com' LIMIT 1), 'HUMANITARIAN', 'Emergency relief, food security, shelter programs', 21000.00, 11, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'care@example.com' LIMIT 1), 'HUMANITARIAN', 'International humanitarian organization, emergency relief', 16000.00, 8, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'oxfam@example.com' LIMIT 1), 'POVERTY', 'Economic justice, fair trade, sustainable development', 17000.00, 9, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'savethechildren@example.com' LIMIT 1), 'CHILDREN', 'Child welfare, education access, health programs for children', 20000.00, 12, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'plan@example.com' LIMIT 1), 'CHILDREN', 'Girls education, gender equality, youth empowerment', 15000.00, 8, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'medecins@example.com' LIMIT 1), 'HEALTHCARE', 'Medical care for vulnerable populations, health education', 23000.00, 14, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'secours@example.com' LIMIT 1), 'SOCIAL_JUSTICE', 'Social inclusion, solidarity programs, community support', 13000.00, 7, NOW(), NOW());

-- Insert Sponsors (15 sponsors)
INSERT INTO sponsors (sponsor_id, user_id, payment_method, sponcer_cat, total_amount_spent, total_sponsorships, created_at, updated_at) VALUES
(UUID(), (SELECT user_id FROM users WHERE email = 'john.doe@company.com' LIMIT 1), 'CREDIT_CARD', 'TECHNOLOGY', 8500.00, 5, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'marie.dupont@enterprise.com' LIMIT 1), 'BANK_TRANSFER', 'ENVIRONMENT', 12000.00, 7, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'pierre.martin@startup.com' LIMIT 1), 'PAYPAL', 'EDUCATION', 6500.00, 4, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'sophie.bernard@corp.com' LIMIT 1), 'CREDIT_CARD', 'HEALTHCARE', 9500.00, 6, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'alexandre.leroy@tech.com' LIMIT 1), 'BANK_TRANSFER', 'TECHNOLOGY', 11000.00, 8, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'emma.roussel@finance.com' LIMIT 1), 'CREDIT_CARD', 'FINANCE', 7800.00, 5, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'thomas.moreau@consulting.com' LIMIT 1), 'BANK_TRANSFER', 'CONSULTING', 9200.00, 6, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'lucie.petit@retail.com' LIMIT 1), 'PAYPAL', 'RETAIL', 6800.00, 4, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'antoine.lefevre@manufacturing.com' LIMIT 1), 'CREDIT_CARD', 'MANUFACTURING', 10500.00, 7, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'camille.roy@pharma.com' LIMIT 1), 'BANK_TRANSFER', 'PHARMACEUTICAL', 13500.00, 9, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'julien.blanc@energy.com' LIMIT 1), 'CREDIT_CARD', 'ENERGY', 8900.00, 6, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'nathalie.girard@education.com' LIMIT 1), 'PAYPAL', 'EDUCATION', 7200.00, 5, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'sebastien.mercier@realestate.com' LIMIT 1), 'BANK_TRANSFER', 'REAL_ESTATE', 9800.00, 6, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'isabelle.duval@hospitality.com' LIMIT 1), 'CREDIT_CARD', 'HOSPITALITY', 8200.00, 5, NOW(), NOW()),
(UUID(), (SELECT user_id FROM users WHERE email = 'francois.chevalier@law.com' LIMIT 1), 'BANK_TRANSFER', 'LEGAL', 11500.00, 8, NOW(), NOW());

-- Insert Recognition Benefits for each organization (using SELECT to get organization IDs)
INSERT INTO recognition_benefits (organization_id, title, description, amount, currency_type, created_at, updated_at) VALUES
-- Red Cross France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'redcross@example.com')), 'Emergency Response Supporter', 'Support emergency response teams and disaster relief efforts', 100.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'redcross@example.com')), 'Blood Donation Champion', 'Help maintain blood bank supplies for emergency situations', 50.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'redcross@example.com')), 'First Aid Training Supporter', 'Fund first aid training programs for communities', 75.00, 'EUR', NOW(), NOW()),

-- WWF France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'wwf@example.com')), 'Wildlife Protector', 'Support wildlife conservation and habitat protection', 75.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'wwf@example.com')), 'Climate Action Supporter', 'Fund climate change initiatives and renewable energy projects', 150.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'wwf@example.com')), 'Ocean Guardian', 'Protect marine life and ocean ecosystems', 120.00, 'EUR', NOW(), NOW()),

-- UNICEF France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'unicef@example.com')), 'Child Education Supporter', 'Provide education opportunities for underprivileged children', 80.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'unicef@example.com')), 'Child Health Champion', 'Support healthcare services for children in need', 120.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'unicef@example.com')), 'Child Protection Advocate', 'Fund child protection and safety programs', 90.00, 'EUR', NOW(), NOW()),

-- Doctors Without Borders Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctors@example.com')), 'Medical Emergency Fund', 'Support emergency medical services and healthcare access', 200.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctors@example.com')), 'Vaccination Campaign Supporter', 'Fund vaccination campaigns in underserved areas', 90.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctors@example.com')), 'Surgical Mission Supporter', 'Support surgical missions in crisis zones', 150.00, 'EUR', NOW(), NOW()),

-- Amnesty International France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'amnesty@example.com')), 'Human Rights Defender', 'Support human rights advocacy and legal assistance', 110.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'amnesty@example.com')), 'Justice Campaign Supporter', 'Fund campaigns for justice and freedom', 85.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'amnesty@example.com')), 'Prisoner of Conscience Advocate', 'Support prisoners of conscience and political prisoners', 95.00, 'EUR', NOW(), NOW()),

-- Greenpeace France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'greenpeace@example.com')), 'Environmental Protector', 'Support environmental protection campaigns', 100.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'greenpeace@example.com')), 'Anti-Pollution Campaigner', 'Fund anti-pollution and clean energy initiatives', 130.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'greenpeace@example.com')), 'Forest Guardian', 'Protect forests and biodiversity', 70.00, 'EUR', NOW(), NOW()),

-- Handicap International Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'handicap@example.com')), 'Disability Rights Advocate', 'Support disability rights and inclusion programs', 80.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'handicap@example.com')), 'Accessibility Champion', 'Fund accessibility improvements and inclusive design', 95.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'handicap@example.com')), 'Rehabilitation Supporter', 'Support rehabilitation services for people with disabilities', 110.00, 'EUR', NOW(), NOW()),

-- Action Aid France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'actionaid@example.com')), 'Poverty Fighter', 'Support poverty alleviation and community development', 85.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'actionaid@example.com')), 'Women Empowerment Supporter', 'Fund women empowerment and gender equality programs', 105.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'actionaid@example.com')), 'Community Development Advocate', 'Support sustainable community development projects', 120.00, 'EUR', NOW(), NOW()),

-- CARE France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'care@example.com')), 'Emergency Relief Supporter', 'Support emergency relief and disaster response', 140.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'care@example.com')), 'Food Security Champion', 'Fund food security and nutrition programs', 90.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'care@example.com')), 'Shelter Program Supporter', 'Support shelter and housing programs for vulnerable populations', 110.00, 'EUR', NOW(), NOW()),

-- Oxfam France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'oxfam@example.com')), 'Economic Justice Advocate', 'Support economic justice and fair trade initiatives', 95.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'oxfam@example.com')), 'Inequality Fighter', 'Fund campaigns to reduce inequality and poverty', 115.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'oxfam@example.com')), 'Sustainable Development Supporter', 'Support sustainable development and climate justice', 125.00, 'EUR', NOW(), NOW()),

-- Save the Children France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'savethechildren@example.com')), 'Child Welfare Champion', 'Support child welfare and protection programs', 100.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'savethechildren@example.com')), 'Education Access Supporter', 'Fund education access for vulnerable children', 85.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'savethechildren@example.com')), 'Child Health Advocate', 'Support child health and nutrition programs', 110.00, 'EUR', NOW(), NOW()),

-- Plan International France Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'plan@example.com')), 'Girls Education Supporter', 'Support girls education and gender equality', 90.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'plan@example.com')), 'Youth Empowerment Champion', 'Fund youth empowerment and leadership programs', 105.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'plan@example.com')), 'Gender Equality Advocate', 'Support gender equality and women rights', 120.00, 'EUR', NOW(), NOW()),

-- Médecins du Monde Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'medecins@example.com')), 'Medical Care Supporter', 'Support medical care for vulnerable populations', 130.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'medecins@example.com')), 'Health Education Champion', 'Fund health education and prevention programs', 80.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'medecins@example.com')), 'Mental Health Advocate', 'Support mental health services and awareness', 95.00, 'EUR', NOW(), NOW()),

-- Secours Populaire Français Benefits
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'secours@example.com')), 'Solidarity Supporter', 'Support solidarity and social inclusion programs', 75.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'secours@example.com')), 'Social Justice Champion', 'Fund social justice and equality initiatives', 100.00, 'EUR', NOW(), NOW()),
((SELECT company_id FROM companies_non_profits WHERE user_id = (SELECT user_id FROM users WHERE email = 'secours@example.com')), 'Community Support Advocate', 'Support community support and assistance programs', 85.00, 'EUR', NOW(), NOW());

-- Insert some password reset tokens for testing (optional)
INSERT INTO password_reset_tokens (token_id, token, user_id, expiry_date, used) VALUES
(UUID(), 'test-reset-token-123', (SELECT user_id FROM users WHERE email = 'john.doe@company.com' LIMIT 1), DATE_ADD(NOW(), INTERVAL 1 HOUR), false),
(UUID(), 'test-reset-token-456', (SELECT user_id FROM users WHERE email = 'marie.dupont@enterprise.com' LIMIT 1), DATE_ADD(NOW(), INTERVAL 1 HOUR), false);

-- Display summary
SELECT 'Dummy data insertion completed successfully!' as status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_organizations FROM companies_non_profits;
SELECT COUNT(*) as total_sponsors FROM sponsors;
SELECT COUNT(*) as total_recognition_benefits FROM recognition_benefits;

-- Verify all UUIDs are valid
SELECT COUNT(*) as invalid_uuids FROM users WHERE user_id NOT REGEXP '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
SELECT COUNT(*) as invalid_company_uuids FROM companies_non_profits WHERE company_id NOT REGEXP '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
SELECT COUNT(*) as invalid_sponsor_uuids FROM sponsors WHERE sponsor_id NOT REGEXP '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';