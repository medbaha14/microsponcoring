@echo off
echo Setting up Microsponsoring Database...
echo.

REM Check if MySQL is in PATH
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo MySQL not found in PATH. Checking common installation locations...
    
    REM Try to find MySQL in common locations
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else if exist "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set "MYSQL_PATH=C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else if exist "C:\xampp\mysql\bin\mysql.exe" (
        set "MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe"
    ) else (
        echo MySQL not found. Please install MySQL or add it to your PATH.
        echo You can download MySQL from: https://dev.mysql.com/downloads/mysql/
        echo Or use XAMPP: https://www.apachefriends.org/
        pause
        exit /b 1
    )
) else (
    set "MYSQL_PATH=mysql"
)

echo Found MySQL at: %MYSQL_PATH%
echo.

REM Create the database
echo Creating database...
%MYSQL_PATH% -u root -p -e "CREATE DATABASE IF NOT EXISTS microsponsoring CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if %errorlevel% equ 0 (
    echo Database created successfully!
    echo.
    echo You can now run your Spring Boot application with:
    echo mvn spring-boot:run
) else (
    echo Failed to create database. Please check your MySQL connection.
)

echo.
pause

