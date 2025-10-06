#!/bin/bash

# Microsponsoring Kubernetes Deployment Script
# This script deploys all components in the correct order

set -e

echo "🚀 Starting Microsponsoring Kubernetes Deployment..."

# Create namespace first
echo "📁 Creating namespace..."
kubectl apply -f namespace.yaml

# Create storage class and persistent volume
echo "💾 Creating storage class and persistent volume..."
kubectl apply -f storage-class.yaml

# Wait for storage class to be ready
echo "⏳ Waiting for storage class to be ready..."
sleep 5

# Deploy MySQL first (database dependency)
echo "🗄️ Deploying MySQL..."
kubectl apply -f mysql-deployment.yaml

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy backend (depends on MySQL)
echo "🔧 Deploying backend..."
kubectl apply -f backend-deployment.yaml

# Deploy frontend
echo "🎨 Deploying frontend..."
kubectl apply -f frontend-deployment.yaml

# Deploy monitoring
echo "📊 Deploying monitoring..."
kubectl apply -f monitoring.yaml

# Deploy secrets
echo "🔐 Deploying secrets..."
kubectl apply -f secrets.yaml

# Deploy ingress last (depends on all services)
echo "🌐 Deploying ingress..."
kubectl apply -f ingress.yaml

# Wait for all deployments to be ready
echo "⏳ Waiting for all deployments to be ready..."
kubectl wait --for=condition=available deployment --all -n microsponsoring --timeout=300s

# Check status
echo "✅ Deployment completed! Checking status..."
echo ""
echo "📋 Pod Status:"
kubectl get pods -n microsponsoring
echo ""
echo "🔗 Services:"
kubectl get services -n microsponsoring
echo ""
echo "💾 PVCs:"
kubectl get pvc -n microsponsoring
echo ""
echo "🌐 Ingress:"
kubectl get ingress -n microsponsoring

echo ""
echo "🎉 Deployment completed successfully!"
echo ""
echo "📝 To access your application:"
echo "1. Add 'microsponsoring.local' to your /etc/hosts file pointing to your master node IP"
echo "2. Access the application at: http://microsponsoring.local"
echo ""
echo "🔍 To check logs:"
echo "kubectl logs -f deployment/backend-deployment -n microsponsoring"
echo "kubectl logs -f deployment/frontend-deployment -n microsponsoring"
echo "kubectl logs -f deployment/mysql-deployment -n microsponsoring"
