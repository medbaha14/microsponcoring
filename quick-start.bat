@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo 🚀 Starting Microsponsoring Application Deployment...
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker and try again.
    pause
    exit /b 1
)

REM Check if Docker Compose is available
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose is not installed. Please install it and try again.
    pause
    exit /b 1
)

echo [INFO] Building and starting services...
echo.

REM Build and start all services
docker-compose up --build -d

echo [INFO] Waiting for services to be ready...
echo.

REM Wait for MySQL to be ready
echo [INFO] Waiting for MySQL to be ready...
:wait_mysql
docker-compose exec -T mysql mysqladmin ping -h"localhost" --silent >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto wait_mysql
)

REM Wait for backend to be ready
echo [INFO] Waiting for backend to be ready...
:wait_backend
curl -f http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    timeout /t 5 /nobreak >nul
    goto wait_backend
)

REM Wait for frontend to be ready
echo [INFO] Waiting for frontend to be ready...
:wait_frontend
curl -f http://localhost/health >nul 2>&1
if errorlevel 1 (
    timeout /t 5 /nobreak >nul
    goto wait_frontend
)

echo [INFO] All services are ready!
echo.

echo 🎉 Application successfully deployed!
echo.
echo 📱 Frontend: http://localhost
echo 🔧 Backend API: http://localhost:8080
echo 📊 Grafana Dashboard: http://localhost:3000 (admin/admin123)
echo 📈 Prometheus: http://localhost:9090
echo 🗄️  MySQL: localhost:3306
echo 🔴 Redis: localhost:6379
echo.
echo 📋 Useful commands:
echo   - View logs: docker-compose logs -f [service-name]
echo   - Stop services: docker-compose down
echo   - Restart services: docker-compose restart
echo   - View running containers: docker-compose ps
echo.
echo 🔍 Health checks:
echo   - Backend health: curl http://localhost:8080/actuator/health
echo   - Frontend health: curl http://localhost/health
echo.

echo [INFO] Performing health checks...
echo.

REM Backend health check
curl -f http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ❌ Backend: Unhealthy
) else (
    echo ✅ Backend: Healthy
)

REM Frontend health check
curl -f http://localhost/health >nul 2>&1
if errorlevel 1 (
    echo ❌ Frontend: Unhealthy
) else (
    echo ✅ Frontend: Healthy
)

REM MySQL health check
docker-compose exec -T mysql mysqladmin ping -h"localhost" --silent >nul 2>&1
if errorlevel 1 (
    echo ❌ MySQL: Unhealthy
) else (
    echo ✅ MySQL: Healthy
)

echo.
echo [INFO] Deployment completed successfully!
echo [INFO] You can now access your application at the URLs shown above.
echo.
pause 