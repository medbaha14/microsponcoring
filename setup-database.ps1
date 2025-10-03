# Setup Database for Microsponsoring Application
# This script helps set up MySQL and create the database

Write-Host "Setting up Microsponsoring Database..." -ForegroundColor Green

# Check if MySQL is running
Write-Host "Checking if MySQL service is running..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue

if ($mysqlService -and $mysqlService.Status -eq "Running") {
    Write-Host "MySQL service is already running!" -ForegroundColor Green
} else {
    Write-Host "MySQL service not found or not running." -ForegroundColor Red
    Write-Host "Please ensure MySQL is installed and running on your system." -ForegroundColor Yellow
    Write-Host "You can download MySQL from: https://dev.mysql.com/downloads/mysql/" -ForegroundColor Cyan
    Write-Host "Or use XAMPP: https://www.apachefriends.org/" -ForegroundColor Cyan
    exit 1
}

# Try to connect to MySQL and create database
Write-Host "Attempting to connect to MySQL and create database..." -ForegroundColor Yellow

try {
    # You may need to adjust the path to mysql.exe based on your installation
    $mysqlPath = "mysql.exe"
    
    # Check if mysql is in PATH
    if (-not (Get-Command $mysqlPath -ErrorAction SilentlyContinue)) {
        # Try common MySQL installation paths
        $possiblePaths = @(
            "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
            "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
            "C:\xampp\mysql\bin\mysql.exe"
        )
        
        foreach ($path in $possiblePaths) {
            if (Test-Path $path) {
                $mysqlPath = $path
                break
            }
        }
    }
    
    if (Test-Path $mysqlPath) {
        Write-Host "Found MySQL at: $mysqlPath" -ForegroundColor Green
        
        # Create database using the SQL script
        & $mysqlPath -u root -p -e "source setup-database.sql"
        
        Write-Host "Database setup completed successfully!" -ForegroundColor Green
        Write-Host "You can now run your Spring Boot application." -ForegroundColor Cyan
    } else {
        Write-Host "MySQL executable not found. Please ensure MySQL is properly installed." -ForegroundColor Red
        Write-Host "You may need to add MySQL to your PATH or specify the full path." -ForegroundColor Yellow
    }
} catch {
    Write-Host "Error setting up database: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please ensure MySQL is running and accessible." -ForegroundColor Yellow
}

Write-Host "Setup complete!" -ForegroundColor Green




