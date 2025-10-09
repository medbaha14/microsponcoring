#!/bin/bash

echo "🔧 QUICK FIX - Deploying fixed version with NodePort..."

NAMESPACE="microsponsoring"

echo "📋 Step 1: Clean up current deployments..."
kubectl delete deployment backend-deployment -n $NAMESPACE --ignore-not-found=true
kubectl delete deployment frontend-deployment -n $NAMESPACE --ignore-not-found=true
kubectl delete service backend-loadbalancer -n $NAMESPACE --ignore-not-found=true
kubectl delete service frontend-loadbalancer -n $NAMESPACE --ignore-not-found=true
kubectl delete service image-loadbalancer -n $NAMESPACE --ignore-not-found=true

echo "⏳ Waiting for cleanup..."
sleep 10

echo "🔧 Step 2: Deploy FIXED version..."
kubectl apply -f FIXED_DEPLOYMENT.yaml

echo "⏳ Step 3: Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/backend-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=300s deployment/frontend-deployment -n $NAMESPACE

echo "🔍 Step 4: Check status..."
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE

echo ""
echo "🎉 FIXED DEPLOYMENT COMPLETED!"
echo "📊 Access Information:"
echo "  Frontend: http://<node-ip>:30082"
echo "  API: http://<node-ip>:30080"
echo "  WebSocket: ws://<node-ip>:30081"
echo "  Images: http://<node-ip>:30083"

echo ""
echo "🔍 Get node IP:"
kubectl get nodes -o wide

echo ""
echo "🧪 Test backend health:"
echo "  curl http://<node-ip>:30080/actuator/health"
