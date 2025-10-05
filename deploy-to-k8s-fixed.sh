#!/bin/bash

# Microsponsoring Kubernetes Deployment Script
# This script deploys the microsponsoring application to Kubernetes

set -e

echo "🚀 Starting Microsponsoring Kubernetes Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
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

# Check if namespace exists, create if not
print_status "Creating namespace if it doesn't exist..."
kubectl create namespace microsponsoring --dry-run=client -o yaml | kubectl apply -f -

# Apply secrets
print_status "Applying secrets..."
kubectl apply -f k8s/secrets.yaml

# Deploy MySQL
print_status "Deploying MySQL..."
kubectl apply -f k8s/mysql-deployment.yaml

# Wait for MySQL to be ready
print_status "Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy Backend
print_status "Deploying Backend..."
kubectl apply -f k8s/backend-deployment.yaml

# Wait for Backend to be ready
print_status "Waiting for Backend to be ready..."
kubectl wait --for=condition=ready pod -l app=backend -n microsponsoring --timeout=300s

# Deploy Frontend
print_status "Deploying Frontend..."
kubectl apply -f k8s/frontend-deployment.yaml

# Wait for Frontend to be ready
print_status "Waiting for Frontend to be ready..."
kubectl wait --for=condition=ready pod -l app=frontend -n microsponsoring --timeout=300s

# Deploy Ingress
print_status "Deploying Ingress..."
kubectl apply -f k8s/ingress.yaml

# Deploy Monitoring (optional)
if [ -f "k8s/monitoring.yaml" ]; then
    print_status "Deploying Monitoring..."
    kubectl apply -f k8s/monitoring.yaml
fi

# Get service information
print_status "Getting service information..."
echo ""
echo "📋 Service Information:"
echo "======================"
kubectl get services -n microsponsoring
echo ""
echo "📦 Pod Information:"
echo "==================="
kubectl get pods -n microsponsoring
echo ""

# Check if ingress controller is available
if kubectl get ingress -n microsponsoring microsponsoring-ingress &> /dev/null; then
    print_status "Ingress deployed successfully!"
    echo ""
    echo "🌐 Access Information:"
    echo "====================="
    echo "Frontend: http://microsponsoring.local"
    echo "Backend API: http://microsponsoring.local/api"
    echo "WebSocket: ws://microsponsoring.local/ws-notifications"
    echo ""
    print_warning "Make sure to add 'microsponsoring.local' to your /etc/hosts file:"
    print_warning "echo '127.0.0.1 microsponsoring.local' | sudo tee -a /etc/hosts"
else
    print_warning "Ingress not available. You may need to set up port forwarding:"
    echo ""
    echo "🔧 Port Forwarding Commands:"
    echo "============================"
    echo "Frontend: kubectl port-forward -n microsponsoring svc/frontend-service 8080:80"
    echo "Backend:  kubectl port-forward -n microsponsoring svc/backend-service 8081:80"
    echo ""
    echo "Then access:"
    echo "- Frontend: http://localhost:8080"
    echo "- Backend:  http://localhost:8081"
fi

print_status "Deployment completed successfully! 🎉"

# Show logs for debugging
echo ""
echo "📝 Recent logs (Backend):"
echo "========================="
kubectl logs -n microsponsoring -l app=backend --tail=10

echo ""
echo "📝 Recent logs (Frontend):"
echo "=========================="
kubectl logs -n microsponsoring -l app=frontend --tail=10
