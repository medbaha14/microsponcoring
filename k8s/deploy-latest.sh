#!/bin/bash

# Deploy Latest Version Script
echo "🚀 DEPLOYING LATEST VERSION..."

NAMESPACE="microsponsoring"

echo "📋 Step 1: Force pull latest images..."
# Delete existing deployments to force recreation with latest images
kubectl delete deployment backend-deployment -n $NAMESPACE --ignore-not-found=true
kubectl delete deployment frontend-deployment -n $NAMESPACE --ignore-not-found=true
kubectl delete deployment image-service -n $NAMESPACE --ignore-not-found=true

echo "⏳ Waiting for cleanup..."
sleep 10

echo "🔧 Step 2: Deploy latest version..."
kubectl apply -f FINAL_CLEAN_DEPLOYMENT.yaml

echo "⏳ Step 3: Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=600s deployment/mysql-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/backend-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/frontend-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/image-service -n $NAMESPACE

echo "🔍 Step 4: Check deployment status..."
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE

echo "📊 Step 5: Get access information..."

# Get NodePort IPs and ports
FRONTEND_NODEPORT=$(kubectl get service frontend-nodeport -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}')
BACKEND_NODEPORT=$(kubectl get service backend-nodeport -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}')
BACKEND_WS_NODEPORT=$(kubectl get service backend-nodeport -n $NAMESPACE -o jsonpath='{.spec.ports[1].nodePort}')
IMAGE_NODEPORT=$(kubectl get service image-nodeport -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}')

# Get node IP (assuming single node cluster)
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')

FRONTEND_IP="$NODE_IP:$FRONTEND_NODEPORT"
BACKEND_IP="$NODE_IP:$BACKEND_NODEPORT"
BACKEND_WS_IP="$NODE_IP:$BACKEND_WS_NODEPORT"
IMAGE_IP="$NODE_IP:$IMAGE_NODEPORT"

echo ""
echo "🎉 LATEST VERSION DEPLOYED!"
echo "📊 Access Information:"

if [ -n "$FRONTEND_IP" ]; then
    echo "  Frontend: http://$FRONTEND_IP"
else
    echo "  Frontend: NodePort pending..."
fi

if [ -n "$BACKEND_IP" ]; then
    echo "  API: http://$BACKEND_IP"
    echo "  WebSocket: ws://$BACKEND_WS_IP/ws-notifications"
    echo "  Health: http://$BACKEND_IP/actuator/health"
else
    echo "  Backend: NodePort pending..."
fi

if [ -n "$IMAGE_IP" ]; then
    echo "  Images: http://$IMAGE_IP"
else
    echo "  Images: NodePort pending..."
fi

echo ""
echo "✅ Deployment completed successfully!"
