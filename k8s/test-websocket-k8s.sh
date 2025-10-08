#!/bin/bash

echo "Testing WebSocket connection in Kubernetes..."

# Get the NodePort for the Ingress Controller
INGRESS_NODE_PORT=$(kubectl get services -n ingress-nginx ingress-nginx-controller -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}')
INGRESS_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')

if [ -z "$INGRESS_NODE_PORT" ] || [ -z "$INGRESS_IP" ]; then
    echo "Error: Could not determine Ingress IP or NodePort. Ensure Ingress Controller is running."
    exit 1
fi

WS_URL="ws://microsponsoring.local:$INGRESS_NODE_PORT/ws-notifications"
HTTP_URL="http://microsponsoring.local:$INGRESS_NODE_PORT/ws-notifications"

echo "WebSocket URL: $WS_URL"
echo "HTTP URL: $HTTP_URL"

echo ""
echo "--- Testing with curl (HTTP upgrade request) ---"
curl -v \
     -H "Upgrade: websocket" \
     -H "Connection: Upgrade" \
     -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
     -H "Sec-WebSocket-Version: 13" \
     -H "Host: microsponsoring.local" \
     "$HTTP_URL"

echo ""
echo "--- Checking backend service endpoints ---"
kubectl get endpoints backend-service -n microsponsoring

echo ""
echo "--- Checking backend logs for WebSocket activity ---"
kubectl logs -l app=backend -n microsponsoring --tail=20 | grep -i websocket || echo "No WebSocket activity in backend logs."

echo ""
echo "--- Testing WebSocket with wscat (if available) ---"
if command -v wscat &> /dev/null; then
    echo "wscat found. Testing connection..."
    timeout 10s wscat -c "$WS_URL" || echo "WebSocket connection test completed"
else
    echo "wscat not found. Install with: npm install -g wscat"
fi

echo ""
echo "--- Checking ingress status ---"
kubectl get ingress microsponsoring-ingress -n microsponsoring

echo ""
echo "WebSocket test completed."
