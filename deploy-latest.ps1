# Microsponsoring Kubernetes Deployment Script (PowerShell)
# This script pulls the latest Docker images and deploys them to Kubernetes

param(
    [switch]$Force = $false
)

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

Write-Host "🚀 Starting Microsponsoring Deployment..." -ForegroundColor Cyan

# Check if kubectl is available
try {
    $kubectlVersion = kubectl version --client --short 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "kubectl not found"
    }
    Write-Success "kubectl is available"
} catch {
    Write-Error "kubectl is not installed or not in PATH"
    exit 1
}

# Check if kubectl can connect to cluster
Write-Status "Checking Kubernetes cluster connection..."
try {
    $clusterInfo = kubectl cluster-info 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot connect to cluster"
    }
    Write-Success "Connected to Kubernetes cluster"
} catch {
    Write-Error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
}

# Get current context
$currentContext = kubectl config current-context
Write-Status "Current context: $currentContext"

# Check if namespace exists
Write-Status "Checking if namespace 'microsponsoring' exists..."
$namespaceExists = kubectl get namespace microsponsoring 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Namespace 'microsponsoring' does not exist. Creating it..."
    kubectl apply -f k8s/namespace.yaml
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Namespace created"
    } else {
        Write-Error "Failed to create namespace"
        exit 1
    }
} else {
    Write-Success "Namespace 'microsponsoring' exists"
}

# Function to deploy image
function Deploy-Image {
    param(
        [string]$ImageName,
        [string]$DeploymentName,
        [string]$Namespace
    )
    
    Write-Status "Deploying $DeploymentName with image: $ImageName"
    
    # Update the deployment to force pull of latest image
    Write-Status "Updating deployment $DeploymentName..."
    $patchJson = @{
        spec = @{
            template = @{
                spec = @{
                    containers = @(
                        @{
                            name = $DeploymentName
                            image = $ImageName
                            imagePullPolicy = "Always"
                        }
                    )
                }
            }
        }
    } | ConvertTo-Json -Depth 10
    
    kubectl patch deployment $DeploymentName -n $Namespace --patch $patchJson
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Waiting for rollout to complete..."
        kubectl rollout status deployment/$DeploymentName -n $Namespace --timeout=300s
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "$DeploymentName deployment completed"
        } else {
            Write-Error "Rollout failed for $DeploymentName"
            return $false
        }
    } else {
        Write-Error "Failed to update deployment $DeploymentName"
        return $false
    }
    return $true
}

# Deploy backend
Write-Status "Starting backend deployment..."
$backendSuccess = Deploy-Image "medbaha/pfebackend:lastVer" "backend-deployment" "microsponsoring"

# Deploy frontend
Write-Status "Starting frontend deployment..."
$frontendSuccess = Deploy-Image "medbaha/pfefrontend:lastVer" "frontend-deployment" "microsponsoring"

# Apply secrets if they exist
if (Test-Path "k8s/secrets.yaml") {
    Write-Status "Applying secrets..."
    kubectl apply -f k8s/secrets.yaml
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Secrets applied"
    } else {
        Write-Warning "Failed to apply secrets"
    }
} else {
    Write-Warning "No secrets.yaml found. Make sure your secrets are configured."
}

# Apply monitoring if it exists
if (Test-Path "k8s/monitoring.yaml") {
    Write-Status "Applying monitoring configuration..."
    kubectl apply -f k8s/monitoring.yaml
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Monitoring applied"
    } else {
        Write-Warning "Failed to apply monitoring"
    }
}

# Show deployment status
Write-Status "Deployment Status:"
Write-Host "====================" -ForegroundColor Cyan
kubectl get pods -n microsponsoring
Write-Host ""
kubectl get services -n microsponsoring
Write-Host ""

# Show ingress status
Write-Status "Ingress Status:"
Write-Host "==================" -ForegroundColor Cyan
kubectl get ingress -n microsponsoring
Write-Host ""

# Show replica status
Write-Status "Replica Status:"
Write-Host "==================" -ForegroundColor Cyan
kubectl get deployments -n microsponsoring
Write-Host ""

if ($backendSuccess -and $frontendSuccess) {
    Write-Success "🎉 Deployment completed successfully!"
    Write-Status "Your application should be available at your configured domain."
    Write-Status "To check logs: kubectl logs -f deployment/backend-deployment -n microsponsoring"
    Write-Status "To check logs: kubectl logs -f deployment/frontend-deployment -n microsponsoring"
} else {
    Write-Error "Deployment completed with errors. Please check the logs above."
    exit 1
}
