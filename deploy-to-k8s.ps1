# Deploy Microsponsoring Backend to Kubernetes
# This script should be run on your Kubernetes master node

Write-Host "=== Microsponsoring Backend Kubernetes Deployment ===" -ForegroundColor Green

# Check if kubectl is available
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Error "kubectl is not installed or not in PATH. Please install kubectl first."
    exit 1
}

# Check if we're in the correct directory
if (-not (Test-Path "k8s")) {
    Write-Error "k8s directory not found. Please run this script from the project root directory."
    exit 1
}

Write-Host "1. Checking Kubernetes cluster status..." -ForegroundColor Yellow
kubectl cluster-info

Write-Host "`n2. Creating namespace if it doesn't exist..." -ForegroundColor Yellow
kubectl apply -f k8s/namespace.yaml

Write-Host "`n3. Creating secrets for database credentials..." -ForegroundColor Yellow
kubectl apply -f k8s/secrets.yaml

Write-Host "`n4. Deploying microsponsoring backend application..." -ForegroundColor Yellow
kubectl apply -f k8s/backend-deployment.yaml

Write-Host "`n5. Waiting for application to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=available deployment/backend-deployment -n microsponsoring --timeout=300s

Write-Host "`n6. Checking deployment status..." -ForegroundColor Yellow
kubectl get pods -n microsponsoring
kubectl get services -n microsponsoring

Write-Host "`n7. Checking application health..." -ForegroundColor Yellow
$backendPod = kubectl get pods -n microsponsoring -l app=backend -o jsonpath='{.items[0].metadata.name}'
if ($backendPod) {
    Write-Host "Backend pod: $backendPod" -ForegroundColor Cyan
    kubectl logs $backendPod -n microsponsoring --tail=20
} else {
    Write-Warning "No backend pod found"
}

Write-Host "`n=== Deployment completed! ===" -ForegroundColor Green
Write-Host "You can check the application logs with:" -ForegroundColor Cyan
Write-Host "kubectl logs -f deployment/backend-deployment -n microsponsoring" -ForegroundColor White

Write-Host "`nTo access the application, check the service:" -ForegroundColor Cyan
Write-Host "kubectl get service backend-service -n microsponsoring" -ForegroundColor White

Write-Host "`nTo port-forward for local testing:" -ForegroundColor Cyan
Write-Host "kubectl port-forward service/backend-service 8080:80 -n microsponsoring" -ForegroundColor White
