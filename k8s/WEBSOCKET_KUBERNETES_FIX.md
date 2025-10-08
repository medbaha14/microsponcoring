# WebSocket Kubernetes Configuration Fix

## Problem Analysis
Your WebSocket implementation works locally but fails in Kubernetes due to several configuration issues:

1. **Invalid ingress configuration-snippet** - The `configuration-snippet` annotation was disabled
2. **Incorrect WebSocket routing** - Routing to non-existent `websocket-service`
3. **Missing WebSocket-specific headers** - No proper WebSocket upgrade headers
4. **Database connection issue** - Using wrong service name

## Applied Fixes

### 1. Fixed Ingress Configuration (`ingress.yaml`)
- Removed invalid `configuration-snippet` annotation
- Fixed WebSocket routing to point to `backend-service:8080` instead of `websocket-service`
- Added proper WebSocket annotations:
  - `nginx.ingress.kubernetes.io/proxy-http-version: "1.1"`
  - `nginx.ingress.kubernetes.io/proxy-set-headers: "microsponsoring/websocket-headers"`

### 2. Created WebSocket Headers ConfigMap (`websocket-headers.yaml`)
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: websocket-headers
  namespace: microsponsoring
data:
  Upgrade: "$http_upgrade"
  Connection: "$connection_upgrade"
  Host: "$host"
  X-Real-IP: "$remote_addr"
  X-Forwarded-For: "$proxy_add_x_forwarded_for"
  X-Forwarded-Proto: "$scheme"
```

### 3. Fixed Backend Database Connection (`backend-deployment.yaml`)
- Changed `DB_URL` from `mysql:3306` to `mysql-service:3306`

### 4. Updated Deployment Scripts
- Added WebSocket headers ConfigMap creation to both `deploy-fixed.sh` and `deploy-fixed.ps1`

### 5. Created WebSocket Test Script (`test-websocket-k8s.sh`)
- Comprehensive testing script for WebSocket connectivity
- Tests both HTTP upgrade and WebSocket connection

## WebSocket Endpoints

Your Spring Boot WebSocket configuration provides these endpoints:

### STOMP Endpoints
- **Connection**: `ws://microsponsoring.local:32403/ws-notifications`
- **Message Mappings**:
  - `/app/notification.send` - Send notifications
  - `/app/notification.subscribe` - Subscribe to notifications
  - `/app/notification.ping` - Ping/pong test

### Topics/Queues
- `/topic/notifications` - Broadcast notifications
- `/topic/pong` - Ping responses
- `/user/queue/notifications` - User-specific notifications

## Deployment Steps

1. **Apply the fixes**:
   ```bash
   # On your Kubernetes master node
   cd /path/to/your/project/k8s
   ./deploy-fixed.sh
   ```

2. **Test WebSocket connection**:
   ```bash
   ./test-websocket-k8s.sh
   ```

3. **Test from browser**:
   - Open `http://microsponsoring.local:32403/websocket-test.html`
   - Click "Connect" to test WebSocket connection

## Frontend Integration

Your frontend should connect using:
```javascript
const socket = new SockJS('http://microsponsoring.local:32403/ws-notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to notifications
    stompClient.subscribe('/topic/notifications', function (message) {
        const notification = JSON.parse(message.body);
        console.log('Notification:', notification);
    });
});
```

## Troubleshooting

If WebSocket still doesn't work:

1. **Check backend logs**:
   ```bash
   kubectl logs -l app=backend -n microsponsoring --tail=50
   ```

2. **Check ingress logs**:
   ```bash
   kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --tail=50
   ```

3. **Verify service endpoints**:
   ```bash
   kubectl get endpoints backend-service -n microsponsoring
   ```

4. **Test direct backend connection**:
   ```bash
   kubectl port-forward svc/backend-service 8080:80 -n microsponsoring
   # Then test: ws://localhost:8080/ws-notifications
   ```

## Key Differences: Local vs Kubernetes

| Aspect | Local | Kubernetes |
|--------|-------|------------|
| URL | `ws://localhost:8080/ws-notifications` | `ws://microsponsoring.local:32403/ws-notifications` |
| Routing | Direct to Spring Boot | Through Nginx Ingress |
| Headers | Automatic | Manual via ConfigMap |
| Database | `localhost:3306` | `mysql-service:3306` |

The main issue was that Kubernetes requires explicit WebSocket header configuration and proper service routing, which your local setup handled automatically.
