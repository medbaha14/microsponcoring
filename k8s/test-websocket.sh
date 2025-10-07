#!/bin/bash

echo "🔌 Testing WebSocket Connection..."
echo "=================================="

# Test WebSocket endpoint availability
echo "1. Testing WebSocket endpoint availability..."
curl -I http://microsponsoring.local:32403/ws-notifications

echo ""
echo "2. Testing WebSocket upgrade headers..."
curl -H "Upgrade: websocket" \
     -H "Connection: Upgrade" \
     -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
     -H "Sec-WebSocket-Version: 13" \
     -I http://microsponsoring.local:32403/ws-notifications

echo ""
echo "3. Testing WebSocket service directly..."
WEBSOCKET_POD=$(kubectl get pods -n microsponsoring -l app=websocket-proxy -o jsonpath='{.items[0].metadata.name}')
if [ -n "$WEBSOCKET_POD" ]; then
    echo "WebSocket pod: $WEBSOCKET_POD"
    kubectl exec -it $WEBSOCKET_POD -n microsponsoring -- wget -qO- http://localhost/health
else
    echo "WebSocket pod not found!"
fi

echo ""
echo "4. Testing backend WebSocket endpoint directly..."
BACKEND_POD=$(kubectl get pods -n microsponsoring -l app=backend -o jsonpath='{.items[0].metadata.name}')
if [ -n "$BACKEND_POD" ]; then
    echo "Backend pod: $BACKEND_POD"
    kubectl exec -it $BACKEND_POD -n microsponsoring -- wget -qO- http://localhost:8080/actuator/health
else
    echo "Backend pod not found!"
fi

echo ""
echo "5. Checking ingress configuration..."
kubectl describe ingress microsponsoring-ingress -n microsponsoring | grep -A 10 "ws-notifications"

echo ""
echo "6. Checking WebSocket service endpoints..."
kubectl get endpoints websocket-service -n microsponsoring

echo ""
echo "✅ WebSocket test completed!"
echo ""
echo "🔧 To test WebSocket connection from browser:"
echo "   Open browser console and run:"
echo "   const ws = new WebSocket('ws://microsponsoring.local:32403/ws-notifications');"
echo "   ws.onopen = () => console.log('WebSocket connected!');"
echo "   ws.onerror = (error) => console.error('WebSocket error:', error);"
