# Microsponsoring Kubernetes Deployment Script (PowerShell)
# This script deploys the microsponsoring application to Kubernetes

param(
    [switch]$SkipBuild = $false
)

Write-Host "🚀 Starting Microsponsoring Kubernetes Deployment..." -ForegroundColor Green

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Check if kubectl is available
try {
    kubectl version --client | Out-Null
    Write-Status "kubectl is available"
} catch {
    Write-Error "kubectl is not installed or not in PATH"
    exit 1
}

# Check if namespace exists, create if not
Write-Status "Creating namespace if it doesn't exist..."
kubectl create namespace microsponsoring --dry-run=client -o yaml | kubectl apply -f -

# Apply secrets
Write-Status "Applying secrets..."
kubectl apply -f k8s/secrets.yaml

# Deploy MySQL
Write-Status "Deploying MySQL..."
kubectl apply -f k8s/mysql-deployment.yaml

# Wait for MySQL to be ready
Write-Status "Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy Backend
Write-Status "Deploying Backend..."
kubectl apply -f k8s/backend-deployment.yaml

# Wait for Backend to be ready
Write-Status "Waiting for Backend to be ready..."
kubectl wait --for=condition=ready pod -l app=backend -n microsponsoring --timeout=300s

# Deploy Frontend
Write-Status "Deploying Frontend..."
kubectl apply -f k8s/frontend-deployment.yaml

# Wait for Frontend to be ready
Write-Status "Waiting for Frontend to be ready..."
kubectl wait --for=condition=ready pod -l app=frontend -n microsponsoring --timeout=300s

# Deploy Ingress
Write-Status "Deploying Ingress..."
kubectl apply -f k8s/ingress.yaml

# Deploy Monitoring (optional)
if (Test-Path "k8s/monitoring.yaml") {
    Write-Status "Deploying Monitoring..."
    kubectl apply -f k8s/monitoring.yaml
}

# Get service information
Write-Status "Getting service information..."
Write-Host ""
Write-Host "📋 Service Information:" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan
kubectl get services -n microsponsoring
Write-Host ""
Write-Host "📦 Pod Information:" -ForegroundColor Cyan
Write-Host "===================" -ForegroundColor Cyan
kubectl get pods -n microsponsoring
Write-Host ""

# Check if ingress controller is available
try {
    kubectl get ingress -n microsponsoring microsponsoring-ingress | Out-Null
    Write-Status "Ingress deployed successfully!"
    Write-Host ""
    Write-Host "🌐 Access Information:" -ForegroundColor Cyan
    Write-Host "=====================" -ForegroundColor Cyan
    Write-Host "Frontend: http://microsponsoring.local"
    Write-Host "Backend API: http://microsponsoring.local/api"
    Write-Host "WebSocket: ws://microsponsoring.local/ws-notifications"
    Write-Host ""
    Write-Warning "Make sure to add 'microsponsoring.local' to your hosts file:"
    Write-Warning "Add this line to C:\Windows\System32\drivers\etc\hosts:"
    Write-Warning "127.0.0.1 microsponsoring.local"
} catch {
    Write-Warning "Ingress not available. You may need to set up port forwarding:"
    Write-Host ""
    Write-Host "🔧 Port Forwarding Commands:" -ForegroundColor Cyan
    Write-Host "============================" -ForegroundColor Cyan
    Write-Host "Frontend: kubectl port-forward -n microsponsoring svc/frontend-service 8080:80"
    Write-Host "Backend:  kubectl port-forward -n microsponsoring svc/backend-service 8081:80"
    Write-Host ""
    Write-Host "Then access:"
    Write-Host "- Frontend: http://localhost:8080"
    Write-Host "- Backend:  http://localhost:8081"
}

Write-Status "Deployment completed successfully! 🎉"

# Show logs for debugging
Write-Host ""
Write-Host "📝 Recent logs (Backend):" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan
kubectl logs -n microsponsoring -l app=backend --tail=10

Write-Host ""
Write-Host "📝 Recent logs (Frontend):" -ForegroundColor Cyan
Write-Host "==========================" -ForegroundColor Cyan
kubectl logs -n microsponsoring -l app=frontend --tail=10
