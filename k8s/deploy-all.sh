#!/bin/bash
echo "Starting Microsponsoring deployment from k8s directory..."

# Pull latest Docker images
echo "Pulling latest Docker images..."
docker pull medbaha/pfebackend:latest
docker pull medbaha/pfefrontend:latest
docker pull mysql:8.0

echo "Docker images pulled successfully!"

# Clean up existing deployments that might be causing conflicts
echo "Cleaning up existing deployments..."
kubectl delete deployment backend-deployment frontend-deployment mysql -n microsponsoring --ignore-not-found=true
kubectl delete pvc mysql-pvc images-pvc invoices-pvc -n microsponsoring --ignore-not-found=true
sleep 10

# Create namespace
echo "Creating namespace..."
kubectl apply -f namespace.yaml

# Create secrets
echo "Creating secrets..."
kubectl apply -f secrets.yaml

# Deploy storage first
echo "Deploying storage..."
kubectl apply -f storage-class.yaml

# Deploy MySQL first
echo "Deploying MySQL..."
kubectl apply -f mysql-deployment.yaml

# Wait for MySQL to be ready
echo "Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy backend
echo "Deploying Backend..."
kubectl apply -f backend-deployment.yaml

# Wait for backend to initialize with database
echo "Waiting for backend to connect to database..."
sleep 60

# Check backend logs to see if it started successfully
echo "Checking backend logs..."
kubectl logs deployment/backend-deployment -n microsponsoring --tail=20

# Deploy frontend
echo "Deploying Frontend..."
kubectl apply -f frontend-deployment.yaml

# Deploy ingress
echo "Deploying Ingress..."
kubectl apply -f ingress.yaml

# Deploy monitoring (optional)
echo "Deploying Monitoring..."
kubectl apply -f monitoring.yaml

echo "🎉 Deployment completed!"
echo "📊 Checking status..."
kubectl get all -n microsponsoring

echo ""
echo "🔍 To check logs:"
echo "   Backend: kubectl logs deployment/backend-deployment -n microsponsoring"
echo "   Frontend: kubectl logs deployment/frontend-deployment -n microsponsoring"
echo "   MySQL: kubectl logs deployment/mysql -n microsponsoring"
