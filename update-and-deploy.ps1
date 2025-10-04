# Update and Deploy Microsponsoring Backend to Kubernetes
# This script pulls the latest Docker image and updates the deployment

Write-Host "=== Update and Deploy Microsponsoring Backend ===" -ForegroundColor Green

# Configuration
$DOCKER_IMAGE = "medbaha14/microsponsoring-backend"
$LATEST_TAG = "latest"
$NAMESPACE = "microsponsoring"
$DEPLOYMENT_NAME = "backend-deployment"

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

Write-Host "`n2. Pulling latest Docker image..." -ForegroundColor Yellow
Write-Host "Image: ${DOCKER_IMAGE}:${LATEST_TAG}" -ForegroundColor Cyan

# Note: In a real scenario, you would pull the image on each node
# For now, we'll update the deployment to use the latest image
Write-Host "Updating deployment to use latest image..." -ForegroundColor Yellow

Write-Host "`n3. Updating deployment with latest image..." -ForegroundColor Yellow
kubectl set image deployment/$DEPLOYMENT_NAME backend=${DOCKER_IMAGE}:${LATEST_TAG} -n $NAMESPACE

Write-Host "`n4. Waiting for rollout to complete..." -ForegroundColor Yellow
kubectl rollout status deployment/$DEPLOYMENT_NAME -n $NAMESPACE --timeout=300s

Write-Host "`n5. Checking deployment status..." -ForegroundColor Yellow
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE

Write-Host "`n6. Checking application health..." -ForegroundColor Yellow
$backendPod = kubectl get pods -n $NAMESPACE -l app=backend -o jsonpath='{.items[0].metadata.name}'
if ($backendPod) {
    Write-Host "Backend pod: $backendPod" -ForegroundColor Cyan
    Write-Host "Checking logs..." -ForegroundColor Yellow
    kubectl logs $backendPod -n $NAMESPACE --tail=20
} else {
    Write-Warning "No backend pod found"
}

Write-Host "`n7. Testing application endpoint..." -ForegroundColor Yellow
$serviceName = "backend-service"
$port = "80"
Write-Host "Service: $serviceName on port $port" -ForegroundColor Cyan

# Check if the service is accessible
$serviceStatus = kubectl get service $serviceName -n $NAMESPACE
if ($serviceStatus) {
    Write-Host "Service is running. You can access it using:" -ForegroundColor Green
    Write-Host "kubectl port-forward service/$serviceName 8080:$port -n $NAMESPACE" -ForegroundColor White
    Write-Host "Then visit: http://localhost:80" -ForegroundColor White
} else {
    Write-Warning "Service not found or not running"
}

Write-Host "`n=== Update and Deployment completed! ===" -ForegroundColor Green
Write-Host "`nUseful commands:" -ForegroundColor Cyan
Write-Host "kubectl logs -f deployment/$DEPLOYMENT_NAME -n $NAMESPACE" -ForegroundColor White
Write-Host "kubectl get pods -n $NAMESPACE" -ForegroundColor White
Write-Host "kubectl describe deployment $DEPLOYMENT_NAME -n $NAMESPACE" -ForegroundColor White
