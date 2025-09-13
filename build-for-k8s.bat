@echo off
echo Building Frontend for Kubernetes Deployment
echo =========================================
echo.

echo Step 1: Updating build information...
call update-build-info.bat
if %errorlevel% neq 0 (
    echo ❌ Failed to update build information
    pause
    exit /b 1
)
echo ✓ Build information updated

echo.
echo Step 2: Installing dependencies...
npm ci --legacy-peer-deps
if %errorlevel% neq 0 (
    echo ❌ Failed to install dependencies
    pause
    exit /b 1
)
echo ✓ Dependencies installed

echo.
echo Step 3: Building with pod configuration...
npm run build -- --configuration pod
echo.
echo Note: The Security Dashboard now includes comprehensive admin features:
echo - Build information and system metrics
echo - User statistics and login monitoring  
echo - Resource usage with progress bars
echo - Quick admin action buttons
echo - Responsive design for all devices
if %errorlevel% neq 0 (
    echo ❌ Build failed
    pause
    exit /b 1
)
echo ✓ Build completed successfully

echo.
echo Step 4: Building Docker image...
docker build -t microsponsoring-frontend:latest .
if %errorlevel% neq 0 (
    echo ❌ Docker build failed
    pause
    exit /b 1
)
echo ✓ Docker image built successfully

echo.
echo Step 5: Tagging for your registry...
echo Please update the following command with your registry:
echo docker tag microsponsoring-frontend:latest YOUR_REGISTRY/microsponsoring-frontend:latest
echo docker push YOUR_REGISTRY/microsponsoring-frontend:latest
echo.

echo Build completed! The Security tab should now be visible in Kubernetes.
echo.
echo To deploy:
echo 1. Push the image to your registry
echo 2. Update k8s/frontend-deployment.yaml with the new image tag
echo 3. Apply the deployment: kubectl apply -f k8s/frontend-deployment.yaml
echo.
pause
