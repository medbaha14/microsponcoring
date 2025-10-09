#!/bin/bash

# Bash script to clean up everything and deploy the final clean version
echo "🧹 CLEANING UP ALL RESOURCES AND DEPLOYING FINAL VERSION..." 

NAMESPACE="microsponsoring"

echo "📋 Step 1: Clean up ALL existing resources..."
# Delete all deployments
kubectl delete deployment --all -n $NAMESPACE --ignore-not-found=true

# Delete all services
kubectl delete service --all -n $NAMESPACE --ignore-not-found=true

# Delete all ingress
kubectl delete ingress --all -n $NAMESPACE --ignore-not-found=true

# Delete all configmaps
kubectl delete configmap --all -n $NAMESPACE --ignore-not-found=true

# Delete all secrets
kubectl delete secret --all -n $NAMESPACE --ignore-not-found=true

# Delete all persistent volume claims
kubectl delete pvc --all -n $NAMESPACE --ignore-not-found=true

echo "⏳ Waiting for cleanup to complete..."
sleep 30

echo "🔧 Step 2: Deploy FINAL CLEAN VERSION..."
kubectl apply -f FINAL_CLEAN_DEPLOYMENT.yaml

echo "⏳ Step 3: Waiting for all deployments to be ready..."
kubectl wait --for=condition=available --timeout=600s deployment/mysql-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/backend-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/frontend-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/image-service -n $NAMESPACE

echo "🔍 Step 4: Check deployment status..."
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE

echo "⏳ Step 5: Wait for LoadBalancer IPs..."
sleep 60

echo "📊 Step 6: Get access information..."

# Get LoadBalancer IPs
FRONTEND_IP=$(kubectl get service frontend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
BACKEND_IP=$(kubectl get service backend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
IMAGE_IP=$(kubectl get service image-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

if [ -z "$FRONTEND_IP" ]; then
    FRONTEND_IP=$(kubectl get service frontend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
fi
if [ -z "$BACKEND_IP" ]; then
    BACKEND_IP=$(kubectl get service backend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
fi
if [ -z "$IMAGE_IP" ]; then
    IMAGE_IP=$(kubectl get service image-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
fi

echo ""
echo "🎉 FINAL DEPLOYMENT COMPLETED!"
echo "📊 Access Information:"

if [ -n "$FRONTEND_IP" ]; then
    echo "  Frontend: http://$FRONTEND_IP"
else
    echo "  Frontend: External IP pending..."
fi

if [ -n "$BACKEND_IP" ]; then
    echo "  API: http://$BACKEND_IP:8080"
    echo "  WebSocket: ws://$BACKEND_IP:8081"
    echo "  Health: http://$BACKEND_IP:8080/api/actuator/health"
else
    echo "  Backend: External IP pending..."
fi

if [ -n "$IMAGE_IP" ]; then
    echo "  Images: http://$IMAGE_IP"
else
    echo "  Images: External IP pending..."
fi

echo ""
echo "🔍 Current status:"
kubectl get all -n $NAMESPACE

echo ""
echo "📝 What was cleaned up:"
echo "  ❌ All old deployments"
echo "  ❌ All old services"
echo "  ❌ All ingress resources"
echo "  ❌ All configmaps"
echo "  ❌ All secrets"
echo "  ❌ All PVCs"

echo ""
echo "✅ What was deployed:"
echo "  ✅ MySQL (1 replica)"
echo "  ✅ Backend (2 replicas) with LoadBalancer"
echo "  ✅ Frontend (1 replica) with LoadBalancer"
echo "  ✅ Image Service (1 replica) with LoadBalancer"
echo "  ✅ Internal services for communication"

echo ""
echo "💡 Next steps:"
echo "1. Update frontend configuration with LoadBalancer IPs"
echo "2. Test all services"
echo "3. Rebuild frontend if needed"

echo ""
echo "🧪 Test commands:"
echo "  kubectl get pods -n $NAMESPACE"
echo "  kubectl get services -n $NAMESPACE"
echo "  kubectl logs -l app=backend -n $NAMESPACE --tail=50"
