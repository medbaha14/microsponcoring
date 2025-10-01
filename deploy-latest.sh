#!/bin/bash

# Microsponsoring Kubernetes Deployment Script
# This script pulls the latest Docker images and deploys them to Kubernetes

set -e  # Exit on any error

echo "🚀 Starting Microsponsoring Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if kubectl can connect to cluster
print_status "Checking Kubernetes cluster connection..."
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi

print_success "Connected to Kubernetes cluster"

# Get current context
CURRENT_CONTEXT=$(kubectl config current-context)
print_status "Current context: $CURRENT_CONTEXT"

# Check if namespace exists
print_status "Checking if namespace 'microsponsoring' exists..."
if ! kubectl get namespace microsponsoring &> /dev/null; then
    print_warning "Namespace 'microsponsoring' does not exist. Creating it..."
    kubectl apply -f k8s/namespace.yaml
    print_success "Namespace created"
else
    print_success "Namespace 'microsponsoring' exists"
fi

# Function to pull and deploy image
deploy_image() {
    local image_name=$1
    local deployment_name=$2
    local namespace=$3
    
    print_status "Deploying $deployment_name with image: $image_name"
    
    # Pull the latest image (this will be done by Kubernetes when we update the deployment)
    print_status "Updating deployment $deployment_name..."
    
    # Update the deployment to force pull of latest image
    kubectl patch deployment $deployment_name -n $namespace -p '{"spec":{"template":{"spec":{"containers":[{"name":"'$deployment_name'","image":"'$image_name'","imagePullPolicy":"Always"}]}}}}'
    
    # Wait for rollout to complete
    print_status "Waiting for rollout to complete..."
    kubectl rollout status deployment/$deployment_name -n $namespace --timeout=300s
    
    print_success "$deployment_name deployment completed"
}

# Deploy backend
print_status "Starting backend deployment..."
deploy_image "medbaha/pfebackend:lastVer" "backend-deployment" "microsponsoring"

# Deploy frontend
print_status "Starting frontend deployment..."
deploy_image "medbaha/pfefrontend:lastVer" "frontend-deployment" "microsponsoring"

# Apply secrets if they exist
if [ -f "k8s/secrets.yaml" ]; then
    print_status "Applying secrets..."
    kubectl apply -f k8s/secrets.yaml
    print_success "Secrets applied"
else
    print_warning "No secrets.yaml found. Make sure your secrets are configured."
fi

# Apply monitoring if it exists
if [ -f "k8s/monitoring.yaml" ]; then
    print_status "Applying monitoring configuration..."
    kubectl apply -f k8s/monitoring.yaml
    print_success "Monitoring applied"
fi

# Show deployment status
print_status "Deployment Status:"
echo "===================="
kubectl get pods -n microsponsoring
echo ""
kubectl get services -n microsponsoring
echo ""

# Show ingress status
print_status "Ingress Status:"
echo "=================="
kubectl get ingress -n microsponsoring
echo ""

# Show replica status
print_status "Replica Status:"
echo "=================="
kubectl get deployments -n microsponsoring
echo ""

print_success "🎉 Deployment completed successfully!"
print_status "Your application should be available at your configured domain."
print_status "To check logs: kubectl logs -f deployment/backend-deployment -n microsponsoring"
print_status "To check logs: kubectl logs -f deployment/frontend-deployment -n microsponsoring"
