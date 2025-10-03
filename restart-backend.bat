@echo off
echo.
echo ============================================
echo   RESTARTING MICROSPONSORING BACKEND
echo ============================================
echo.
echo Stopping any running backend processes...
taskkill /F /IM java.exe 2>nul
echo.
echo Starting backend with updated configurations...
echo.
cd microsponsoring-backend
mvn spring-boot:run
