#!/bin/bash

# Deploy Microsponsoring Backend to Kubernetes
# This script should be run on your Kubernetes master node

echo "=== Microsponsoring Backend Kubernetes Deployment ==="

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "Error: kubectl is not installed or not in PATH. Please install kubectl first."
    exit 1
fi

# Check if we're in the correct directory
if [ ! -d "k8s" ]; then
    echo "Error: k8s directory not found. Please run this script from the project root directory."
    exit 1
fi

echo "1. Checking Kubernetes cluster status..."
kubectl cluster-info

echo -e "\n2. Creating namespace if it doesn't exist..."
kubectl apply -f k8s/namespace.yaml

echo -e "\n3. Creating configmap for application configuration..."
kubectl apply -f k8s/configmap.yaml

echo -e "\n4. Creating secret for database credentials..."
kubectl apply -f k8s/secret.yaml

echo -e "\n5. Deploying PostgreSQL database..."
kubectl apply -f k8s/postgres.yaml

echo -e "\n6. Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n microsponsoring --timeout=300s

echo -e "\n7. Deploying Redis cache..."
kubectl apply -f k8s/redis.yaml

echo -e "\n8. Waiting for Redis to be ready..."
kubectl wait --for=condition=ready pod -l app=redis -n microsponsoring --timeout=300s

echo -e "\n9. Deploying microsponsoring backend application..."
kubectl apply -f k8s/deployment.yaml

echo -e "\n10. Deploying service..."
kubectl apply -f k8s/service.yaml

echo -e "\n11. Deploying ingress..."
kubectl apply -f k8s/ingress.yaml

echo -e "\n12. Waiting for application to be ready..."
kubectl wait --for=condition=available deployment/microsponsoring-backend -n microsponsoring --timeout=300s

echo -e "\n13. Checking deployment status..."
kubectl get pods -n microsponsoring
kubectl get services -n microsponsoring
kubectl get ingress -n microsponsoring

echo -e "\n=== Deployment completed! ==="
echo "You can check the application logs with:"
echo "kubectl logs -f deployment/microsponsoring-backend -n microsponsoring"

echo -e "\nTo access the application, check the ingress:"
echo "kubectl get ingress -n microsponsoring"
