#!/bin/bash

# Fixed deployment script for microsponsoring application
# This script addresses all the identified issues

set -e

echo "🚀 Starting fixed deployment of microsponsoring application..."

# Clean up existing deployments that might be causing conflicts
echo "🧹 Cleaning up existing deployments..."
kubectl delete deployment backend-deployment frontend-deployment mysql-deployment image-service -n microsponsoring --ignore-not-found=true
kubectl delete service backend-service frontend-service mysql-service image-service -n microsponsoring --ignore-not-found=true
kubectl delete ingress microsponsoring-ingress -n microsponsoring --ignore-not-found=true
kubectl delete configmap nginx-image-config mysql-config -n microsponsoring --ignore-not-found=true
sleep 10

# Create namespace
echo "📁 Creating namespace..."
kubectl apply -f namespace.yaml

# Create secrets
echo "🔐 Creating secrets..."
kubectl apply -f secrets.yaml

# Create WebSocket headers ConfigMap
echo "🔌 Creating WebSocket headers ConfigMap..."
kubectl apply -f websocket-headers.yaml

# Deploy storage first
echo "💾 Deploying storage..."
kubectl apply -f storage-class.yaml

# Deploy MySQL first
echo "🗄️ Deploying MySQL..."
kubectl apply -f mysql-deployment.yaml

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy backend
echo "🔧 Deploying Backend..."
kubectl apply -f backend-deployment.yaml

# Wait for backend to initialize with database
echo "⏳ Waiting for backend to connect to database..."
sleep 60

# Check backend logs to see if it started successfully
echo "📋 Checking backend logs..."
kubectl logs deployment/backend-deployment -n microsponsoring --tail=20

# Deploy image service
echo "🖼️ Deploying Image Service..."
kubectl apply -f image-service.yaml

# Wait for image service to be ready
echo "⏳ Waiting for image service to be ready..."
kubectl wait --for=condition=ready pod -l app=image-service -n microsponsoring --timeout=300s

# Deploy WebSocket service
echo "🔌 Deploying WebSocket Service..."
kubectl apply -f websocket-service.yaml

# Wait for WebSocket service to be ready
echo "⏳ Waiting for WebSocket service to be ready..."
kubectl wait --for=condition=ready pod -l app=websocket-proxy -n microsponsoring --timeout=300s

# Sync images from backend to image service
echo "🔄 Syncing images from backend to image service..."
./sync-images.sh

# Deploy frontend
echo "🎨 Deploying Frontend..."
kubectl apply -f frontend-deployment.yaml

# Wait for frontend to be ready
echo "⏳ Waiting for frontend to be ready..."
kubectl wait --for=condition=ready pod -l app=frontend -n microsponsoring --timeout=300s

# Deploy ingress
echo "🌐 Deploying Ingress..."
kubectl apply -f ingress.yaml

# Deploy monitoring (optional)
echo "📊 Deploying Monitoring..."
kubectl apply -f monitoring.yaml

# Final status check
echo "✅ Deployment completed! Checking status..."
kubectl get pods -n microsponsoring
kubectl get services -n microsponsoring
kubectl get ingress -n microsponsoring

echo ""
echo "🎉 Deployment completed successfully!"
echo ""
echo "📋 Access URLs:"
echo "   Frontend: http://microsponsoring.local:32403"
echo "   API Health: http://microsponsoring.local:32403/api/actuator/health"
echo "   Images: http://microsponsoring.local:32403/images/"
echo ""
echo "🔧 To test the deployment:"
echo "   curl -I http://microsponsoring.local:32403/api/actuator/health"
echo "   curl -I http://microsponsoring.local:32403/images/"
echo "   curl -I http://microsponsoring.local:32403/"
