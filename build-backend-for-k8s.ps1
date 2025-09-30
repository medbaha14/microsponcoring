Write-Host "Building Backend for Kubernetes Deployment" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Step 1: Cleaning previous build..." -ForegroundColor Yellow
Set-Location microsponsoring-backend
mvn clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to clean project" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ Project cleaned" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Building application..." -ForegroundColor Yellow
mvn package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ Backend built successfully" -ForegroundColor Green

Write-Host ""
Write-Host "Step 3: Building Docker image..." -ForegroundColor Yellow
docker build -t microsponsoring-backend:latest .
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ Docker image built successfully" -ForegroundColor Green

Write-Host ""
Write-Host "Step 4: Tagging for your registry..." -ForegroundColor Yellow
Write-Host "Please update the following command with your registry:" -ForegroundColor Cyan
Write-Host "docker tag microsponsoring-backend:latest YOUR_REGISTRY/microsponsoring-backend:latest" -ForegroundColor Cyan
Write-Host "docker push YOUR_REGISTRY/microsponsoring-backend:latest" -ForegroundColor Cyan
Write-Host ""

Write-Host "Backend build completed!" -ForegroundColor Green
Write-Host ""
Write-Host "To deploy:" -ForegroundColor Cyan
Write-Host "1. Push the image to your registry" -ForegroundColor Cyan
Write-Host "2. Update k8s/backend-deployment.yaml with the new image tag" -ForegroundColor Cyan
Write-Host "3. Apply the deployment: kubectl apply -f k8s/backend-deployment.yaml" -ForegroundColor Cyan
Write-Host ""
Set-Location ..
Read-Host "Press Enter to continue"
