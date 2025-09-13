# Database Setup Guide for Microsponsoring Backend

## Problem
Your Spring Boot application is failing to start with the error:
```
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
```

## Solution
The issue was that your project was missing the main `application.properties` file that Spring Boot needs to configure the database connection.

## What I Fixed

### 1. Created `application.properties`
I've created the missing `application.properties` file in `microsponsoring-backend/src/main/resources/` with:
- MySQL database connection settings
- JPA/Hibernate configuration
- Liquibase configuration
- JWT settings
- Stripe checkout configuration
- File upload settings
- Email configuration
- Password reset settings
- Logging configuration

### 2. Created Database Setup Scripts
- `setup-database.sql` - SQL script to create the database
- `setup-database.bat` - Windows batch file for easy database setup
- `setup-database.ps1` - PowerShell script for database setup

## Prerequisites

### 1. Install MySQL
You need MySQL running on your system. You can:

**Option A: Install MySQL Server**
- Download from: https://dev.mysql.com/downloads/mysql/
- Install and start the MySQL service

**Option B: Use XAMPP (Easier)**
- Download from: https://www.apachefriends.org/
- Start Apache and MySQL services

### 2. Ensure MySQL is Running
- MySQL should be running on `localhost:3306`
- Default username: `root`
- Default password: (empty - you may need to set one)

## Quick Setup Steps

### Step 1: Create the Database
Run one of these commands in your project directory:

**Option A: Use the batch file (Windows)**
```cmd
setup-database.bat
```

**Option B: Use PowerShell**
```powershell
.\setup-database.ps1
```

**Option C: Manual MySQL command**
```sql
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS microsponsoring CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Step 2: Run Your Application
```bash
cd microsponsoring-backend
mvn spring-boot:run
```

## Configuration Details

### Database Connection
- **URL**: `jdbc:mysql://localhost:3306/microsponsoring`
- **Username**: `root`
- **Password**: (empty - change in production)
- **Driver**: `com.mysql.cj.jdbc.Driver`

### JPA Settings
- **DDL Auto**: `validate` (validates schema against entities)
- **Show SQL**: `true` (shows generated SQL queries)
- **Dialect**: MySQL 8.0 compatible

### Liquibase
- **Enabled**: `true`
- **Change Log**: `db/changelog/db.changelog-master.xml`

## Troubleshooting

### 1. MySQL Connection Issues
- Ensure MySQL service is running
- Check if port 3306 is available
- Verify username/password

### 2. Database Not Found
- Run the setup scripts to create the database
- Check if the database name matches in `application.properties`

### 3. Permission Issues
- Ensure your MySQL user has CREATE DATABASE privileges
- Try connecting as root user

### 4. Port Conflicts
- If port 8080 is busy, change `server.port` in `application.properties`
- If MySQL port 3306 is busy, change the port in the database URL

## Security Notes

### Production Changes
Before deploying to production:
1. Change the default JWT secret
2. Set a strong MySQL password
3. Use environment variables for sensitive data
4. Disable debug logging

### Environment Variables
You can override settings using environment variables:
```bash
export SPRING_DATASOURCE_PASSWORD=your_secure_password
export JWT_SECRET=your_secure_jwt_secret
```

## Next Steps

After successful database setup:
1. Your Spring Boot application should start without errors
2. Liquibase will automatically create all required tables
3. The application will be available at `http://localhost:8080`
4. You can access the admin account:
   - Username: `admin`
   - Password: `password`

## Additional Configuration Required

**Before the application will work completely, you need to:**

1. **Stripe API Keys** - Get your test keys from https://stripe.com and update:
   - `checkout.secret.key`
   - `checkout.public.key`

2. **Gmail App Password** - For email functionality:
   - Enable 2FA on your Gmail account
   - Generate an app password
   - Update `spring.mail.password`

3. **JWT Secret** - Generate a secure random string for `jwt.secret`

See `CONFIGURATION_GUIDE.md` for detailed setup instructions.

## Support

If you continue to have issues:
1. Check the application logs for specific error messages
2. Verify MySQL is running and accessible
3. Ensure the database exists and is accessible
4. Check that all required dependencies are in your `pom.xml`
