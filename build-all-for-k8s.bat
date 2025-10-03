@echo off
echo Building Complete Microsponsoring Application for Kubernetes
echo ============================================================
echo.

echo Step 1: Building Backend...
call build-backend-for-k8s.bat
if %errorlevel% neq 0 (
    echo ❌ Backend build failed
    pause
    exit /b 1
)
echo ✓ Backend build completed

echo.
echo Step 2: Building Frontend...
call build-for-k8s.bat
if %errorlevel% neq 0 (
    echo ❌ Frontend build failed
    pause
    exit /b 1
)
echo ✓ Frontend build completed

echo.
echo ============================================================
echo 🎉 Complete build successful!
echo ============================================================
echo.
echo Both applications are now built and ready for Kubernetes deployment:
echo - microsponsoring-backend:latest
echo - microsponsoring-frontend:latest
echo.
echo Next steps:
echo 1. Push images to your container registry
echo 2. Update k8s deployment files with your registry URLs
echo 3. Deploy to Kubernetes using: kubectl apply -f k8s/
echo.
pause
