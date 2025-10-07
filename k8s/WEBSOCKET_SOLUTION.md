# 🔌 WebSocket Solution for Kubernetes

## 🎯 **Problem Solved**
WebSocket connections were failing in the Kubernetes environment due to:
1. Missing WebSocket upgrade headers in ingress
2. Incorrect service port configuration
3. Lack of dedicated WebSocket proxy service

## 🛠️ **Solution Implemented**

### 1. **Enhanced Ingress Configuration** (`ingress.yaml`)
- Added WebSocket-specific nginx annotations
- Configured proper upgrade headers and timeouts
- Added configuration snippet for WebSocket handling

### 2. **Dedicated WebSocket Service** (`websocket-service.yaml`)
- Created separate service for WebSocket traffic
- Added nginx proxy with WebSocket-specific configuration
- Configured proper upstream to backend service

### 3. **Updated Backend Service** (`backend-deployment.yaml`)
- Added explicit port configuration for WebSocket traffic
- Named ports for better service discovery

### 4. **Updated Deployment Scripts**
- Added WebSocket service deployment to both `deploy-fixed.sh` and `deploy-fixed.ps1`
- Included proper waiting periods for service readiness

## 📁 **Files Created/Modified**

### New Files:
- `k8s/websocket-service.yaml` - Dedicated WebSocket service and nginx proxy
- `k8s/test-websocket.sh` - WebSocket connection testing script
- `k8s/WEBSOCKET_SOLUTION.md` - This documentation

### Modified Files:
- `k8s/ingress.yaml` - Added WebSocket annotations and routing
- `k8s/backend-deployment.yaml` - Enhanced service port configuration
- `k8s/deploy-fixed.sh` - Added WebSocket service deployment
- `k8s/deploy-fixed.ps1` - Added WebSocket service deployment

## 🚀 **Deployment Instructions**

### Deploy the Complete Solution:
```bash
# Linux/Mac
cd k8s
chmod +x deploy-fixed.sh test-websocket.sh
./deploy-fixed.sh

# Windows
cd k8s
.\deploy-fixed.ps1
```

### Test WebSocket Connection:
```bash
# Run the test script
./test-websocket.sh

# Or test manually in browser console:
const ws = new WebSocket('ws://microsponsoring.local:32403/ws-notifications');
ws.onopen = () => console.log('WebSocket connected!');
ws.onerror = (error) => console.error('WebSocket error:', error);
```

## 🔧 **Technical Details**

### WebSocket Flow:
1. **Frontend** → `ws://microsponsoring.local:32403/ws-notifications`
2. **Ingress** → Routes to `websocket-service:8080`
3. **WebSocket Service** → Proxies to `backend-service:8080`
4. **Backend** → Handles STOMP WebSocket connections

### Key Configuration:
- **Upgrade Headers**: Properly configured for WebSocket upgrade
- **Timeouts**: Extended timeouts for long-lived connections
- **CORS**: Configured for cross-origin WebSocket connections
- **Authentication**: JWT token passed in WebSocket headers

## 🧪 **Testing**

### 1. **Service Health Check:**
```bash
curl -I http://microsponsoring.local:32403/ws-notifications
```

### 2. **WebSocket Upgrade Test:**
```bash
curl -H "Upgrade: websocket" \
     -H "Connection: Upgrade" \
     -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
     -H "Sec-WebSocket-Version: 13" \
     -I http://microsponsoring.local:32403/ws-notifications
```

### 3. **Browser Console Test:**
```javascript
const ws = new WebSocket('ws://microsponsoring.local:32403/ws-notifications');
ws.onopen = () => console.log('✅ WebSocket connected!');
ws.onmessage = (event) => console.log('📨 Message:', event.data);
ws.onerror = (error) => console.error('❌ WebSocket error:', error);
ws.onclose = (event) => console.log('🔌 WebSocket closed:', event.code, event.reason);
```

## 🎉 **Expected Results**

After deployment, you should see:
- ✅ WebSocket connections establish successfully
- ✅ Real-time notifications working
- ✅ No more "Connection Refused" errors
- ✅ Proper WebSocket upgrade handling

## 🔍 **Troubleshooting**

### If WebSocket still fails:
1. Check pod status: `kubectl get pods -n microsponsoring`
2. Check service endpoints: `kubectl get endpoints -n microsponsoring`
3. Check ingress status: `kubectl describe ingress -n microsponsoring`
4. Check WebSocket service logs: `kubectl logs -l app=websocket-proxy -n microsponsoring`

### Common Issues:
- **Port conflicts**: Ensure no other services use port 8080
- **DNS resolution**: Verify `microsponsoring.local` resolves correctly
- **Firewall**: Check if port 32403 is accessible
- **Authentication**: Ensure JWT tokens are valid

## 📊 **Monitoring**

Monitor WebSocket connections:
```bash
# Check WebSocket service logs
kubectl logs -l app=websocket-proxy -n microsponsoring -f

# Check backend WebSocket logs
kubectl logs -l app=backend -n microsponsoring -f | grep -i websocket

# Check ingress logs
kubectl logs -l app.kubernetes.io/name=ingress-nginx -n ingress-nginx -f
```

---

**🎯 This solution provides a robust, production-ready WebSocket implementation for the microsponsoring application in Kubernetes!**
