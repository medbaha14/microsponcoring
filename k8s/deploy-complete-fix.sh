#!/bin/bash

echo "🚀 Starting complete deployment fix..."

# Set namespace
NAMESPACE="microsponsoring"

# Check if we're in the right directory
if [ ! -f "backend-deployment.yaml" ]; then
    echo "❌ Error: backend-deployment.yaml not found. Please run this script from the k8s directory."
    exit 1
fi

echo "📋 Step 1: Clean up existing resources..."
kubectl delete ingress microsponsoring-ingress -n $NAMESPACE --ignore-not-found=true
kubectl delete deployment backend-deployment -n $NAMESPACE --ignore-not-found=true
kubectl delete service backend-service -n $NAMESPACE --ignore-not-found=true
kubectl delete service websocket-service -n $NAMESPACE --ignore-not-found=true
kubectl delete service websocket-nodeport -n $NAMESPACE --ignore-not-found=true

echo "⏳ Waiting for cleanup to complete..."
sleep 10

echo "🔧 Step 2: Apply backend deployment with fixed configuration..."
kubectl apply -f backend-deployment.yaml

echo "⏳ Waiting for backend to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/backend-deployment -n $NAMESPACE

echo "🔍 Step 3: Check backend status..."
kubectl get pods -n $NAMESPACE -l app=backend
kubectl get services -n $NAMESPACE

echo "🌐 Step 4: Apply simple ingress configuration..."
kubectl apply -f ingress-simple.yaml

echo "⏳ Waiting for ingress to be ready..."
sleep 30

echo "🔍 Step 5: Check ingress status..."
kubectl get ingress -n $NAMESPACE

echo "🧪 Step 6: Test backend connectivity..."
BACKEND_POD=$(kubectl get pods -l app=backend -n $NAMESPACE -o jsonpath='{.items[0].metadata.name}')
if [ ! -z "$BACKEND_POD" ]; then
    echo "Testing backend health endpoint..."
    kubectl exec -it $BACKEND_POD -n $NAMESPACE -- wget -qO- http://localhost:8080/api/actuator/health || echo "Health check failed"
    
    echo "Testing WebSocket endpoint..."
    kubectl exec -it $BACKEND_POD -n $NAMESPACE -- wget -qO- http://localhost:8080/api/ws-notifications || echo "WebSocket check failed"
else
    echo "❌ No backend pod found"
fi

echo "🌍 Step 7: Get access information..."
INGRESS_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
INGRESS_PORT=$(kubectl get service -n ingress-nginx ingress-nginx-controller -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}')

echo ""
echo "🎉 Deployment completed!"
echo "📊 Access Information:"
echo "  Frontend: http://microsponsoring.local:$INGRESS_PORT"
echo "  API: http://microsponsoring.local:$INGRESS_PORT/api"
echo "  WebSocket: ws://microsponsoring.local:$INGRESS_PORT/ws-notifications"
echo "  Health: http://microsponsoring.local:$INGRESS_PORT/actuator/health"
echo ""
echo "🔧 If you need to test locally, add this to your /etc/hosts:"
echo "  $INGRESS_IP microsponsoring.local"
echo ""
echo "📝 To check logs:"
echo "  kubectl logs -l app=backend -n $NAMESPACE --tail=50"
echo "  kubectl logs -l app=frontend -n $NAMESPACE --tail=50"
