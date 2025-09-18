@echo off
echo Updating Build Information
echo =========================

set /p ENVIRONMENT="Enter environment (development/staging/production): "
set /p VERSION="Enter version (e.g., 1.0.0): "
set /p BUILD_NUMBER="Enter build number (e.g., k8s-001): "

echo.
echo Updating build-info.ts with:
echo Environment: %ENVIRONMENT%
echo Version: %VERSION%
echo Build Number: %BUILD_NUMBER%
echo Build Time: %date% %time%
echo.

REM Update build-info.ts
powershell -Command "(Get-Content 'src/environments/build-info.ts') -replace 'environment: ''[^'']*''', 'environment: ''%ENVIRONMENT%''' -replace 'version: ''[^'']*''', 'version: ''%VERSION%''' -replace 'buildNumber: ''[^'']*''', 'buildNumber: ''%BUILD_NUMBER%''' | Set-Content 'src/environments/build-info.ts'"

echo ✓ Build information updated successfully!
echo.
echo You can now run: npm run build -- --configuration pod
pause


