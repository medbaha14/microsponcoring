#!/bin/bash

# Update and Deploy Microsponsoring Backend to Kubernetes
# This script pulls the latest Docker image and updates the deployment

echo "=== Update and Deploy Microsponsoring Backend ==="

# Configuration
DOCKER_IMAGE="medbaha14/microsponsoring-backend"
LATEST_TAG="latest"
NAMESPACE="microsponsoring"
DEPLOYMENT_NAME="backend-deployment"

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

echo -e "\n2. Pulling latest Docker image..."
echo "Image: ${DOCKER_IMAGE}:${LATEST_TAG}"

# Note: In a real scenario, you would pull the image on each node
# For now, we'll update the deployment to use the latest image
echo "Updating deployment to use latest image..."

echo -e "\n3. Updating deployment with latest image..."
kubectl set image deployment/$DEPLOYMENT_NAME backend=${DOCKER_IMAGE}:${LATEST_TAG} -n $NAMESPACE

echo -e "\n4. Waiting for rollout to complete..."
kubectl rollout status deployment/$DEPLOYMENT_NAME -n $NAMESPACE --timeout=300s

echo -e "\n5. Checking deployment status..."
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE

echo -e "\n6. Checking application health..."
BACKEND_POD=$(kubectl get pods -n $NAMESPACE -l app=backend -o jsonpath='{.items[0].metadata.name}')
if [ -n "$BACKEND_POD" ]; then
    echo "Backend pod: $BACKEND_POD"
    echo "Checking logs..."
    kubectl logs $BACKEND_POD -n $NAMESPACE --tail=20
else
    echo "Warning: No backend pod found"
fi

echo -e "\n7. Testing application endpoint..."
SERVICE_NAME="backend-service"
PORT="80"
echo "Service: $SERVICE_NAME on port $PORT"

# Check if the service is accessible
SERVICE_STATUS=$(kubectl get service $SERVICE_NAME -n $NAMESPACE)
if [ -n "$SERVICE_STATUS" ]; then
    echo "Service is running. You can access it using:"
    echo "kubectl port-forward service/$SERVICE_NAME 8080:$PORT -n $NAMESPACE"
    echo "Then visit: http://localhost:8080"
else
    echo "Warning: Service not found or not running"
fi

echo -e "\n=== Update and Deployment completed! ==="
echo -e "\nUseful commands:"
echo "kubectl logs -f deployment/$DEPLOYMENT_NAME -n $NAMESPACE"
echo "kubectl get pods -n $NAMESPACE"
echo "kubectl describe deployment $DEPLOYMENT_NAME -n $NAMESPACE"
