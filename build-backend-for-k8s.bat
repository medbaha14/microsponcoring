@echo off
echo Building Backend for Kubernetes Deployment
echo ==========================================
echo.

echo Step 1: Cleaning previous build...
cd microsponsoring-backend
call mvn clean
if %errorlevel% neq 0 (
    echo ❌ Failed to clean project
    pause
    exit /b 1
)
echo ✓ Project cleaned

echo.
echo Step 2: Building with dependency check (non-failing)...
call mvn package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ Build failed
    pause
    exit /b 1
)
echo ✓ Backend built successfully

echo.
echo Step 3: Building Docker image...
docker build -t microsponsoring-backend:latest .
if %errorlevel% neq 0 (
    echo ❌ Docker build failed
    pause
    exit /b 1
)
echo ✓ Docker image built successfully

echo.
echo Step 4: Tagging for your registry...
echo Please update the following command with your registry:
echo docker tag microsponsoring-backend:latest YOUR_REGISTRY/microsponsoring-backend:latest
echo docker push YOUR_REGISTRY/microsponsoring-backend:latest
echo.

echo Backend build completed!
echo.
echo To deploy:
echo 1. Push the image to your registry
echo 2. Update k8s/backend-deployment.yaml with the new image tag
echo 3. Apply the deployment: kubectl apply -f k8s/backend-deployment.yaml
echo.
cd ..
pause
