@echo off
echo Quick Setup for Microsponsoring Backend
echo ======================================
echo.

echo Step 1: Creating uploads directory...
if not exist "uploads" mkdir uploads
echo ✓ Uploads directory created

echo.
echo Step 2: Checking MySQL connection...
mysql --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ MySQL found in PATH
    echo Creating database...
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS microsponsoring CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
    if %errorlevel% equ 0 (
        echo ✓ Database created successfully
    ) else (
        echo ⚠ Could not create database. You may need to run setup-database.bat first
    )
) else (
    echo ⚠ MySQL not found in PATH. Please install MySQL or run setup-database.bat
)

echo.
echo Step 3: Configuration Summary
echo =============================
echo.
echo Your application.properties is now configured with:
echo ✓ Database connection (MySQL)
echo ✓ JPA/Hibernate settings
echo ✓ Liquibase configuration
echo ✓ JWT settings
echo ✓ Stripe checkout configuration (needs your keys)
echo ✓ File upload settings
echo ✓ Email configuration (needs your Gmail app password)
echo ✓ Password reset settings
echo ✓ Logging configuration
echo.

echo IMPORTANT: Before running the application, you need to:
echo 1. Get your Stripe API keys from https://stripe.com
echo 2. Update checkout.secret.key and checkout.public.key in application.properties
echo 3. Set up Gmail app password for email functionality
echo 4. Ensure MySQL is running on localhost:3306
echo.

echo To start the application:
echo cd microsponsoring-backend
echo mvn spring-boot:run
echo.

pause

