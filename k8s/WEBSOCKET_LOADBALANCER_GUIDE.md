# WebSocket LoadBalancer Guide

## Why LoadBalancer is Better for WebSocket

### Current Issues with NodePort + Ingress:
```
Client → Ingress (nginx) → NodePort → Backend
       ↑ Double proxy, timeouts, complex config
```

### LoadBalancer Benefits:
```
Client → LoadBalancer → Backend
       ↑ Direct connection, better performance
```

## WebSocket LoadBalancer Advantages

### 1. **Direct Connection**
- No double proxy (Ingress → NodePort → Backend)
- Lower latency and better performance
- Fewer connection timeouts

### 2. **Session Affinity**
- WebSocket connections stick to the same backend pod
- Prevents connection drops during load balancing
- Better for real-time applications

### 3. **Simplified Configuration**
- No complex ingress rewrite rules
- No special WebSocket headers needed
- Cleaner, more maintainable setup

### 4. **Better Load Balancing**
- Can distribute WebSocket connections across multiple pods
- Health checks ensure only healthy pods receive traffic
- Automatic failover

### 5. **Production Ready**
- Better suited for production environments
- More reliable than NodePort + Ingress
- Easier to scale and monitor

## Configuration Options

### Option 1: Combined LoadBalancer (Recommended)
```yaml
# Single LoadBalancer for both HTTP and WebSocket
apiVersion: v1
kind: Service
metadata:
  name: websocket-loadbalancer
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
    name: http
  - port: 8081
    targetPort: 8081
    name: websocket
  sessionAffinity: ClientIP  # Important for WebSocket
```

### Option 2: Dedicated WebSocket LoadBalancer
```yaml
# Separate LoadBalancer just for WebSocket
apiVersion: v1
kind: Service
metadata:
  name: websocket-dedicated
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8081
    name: websocket
  sessionAffinity: ClientIP
```

## Deployment Steps

### 1. Deploy WebSocket Optimized Backend
```powershell
kubectl apply -f websocket-optimized-deployment.yaml
```

### 2. Wait for LoadBalancer IP
```powershell
kubectl get services -n microsponsoring
# Wait for EXTERNAL-IP to be assigned
```

### 3. Test WebSocket Connection
```powershell
.\test-websocket-loadbalancer.ps1
```

### 4. Update Frontend Configuration
```typescript
// environment.pod.ts
export const environment = {
  production: true,
  apiUrl: 'http://<loadbalancer-ip>:80/api',
  wsUrl: 'ws://<loadbalancer-ip>:8081/ws-notifications',
  imageUrl: 'http://<loadbalancer-ip>:80/images',
  baseUrl: 'http://<loadbalancer-ip>:80'
};
```

## WebSocket Testing

### Browser Testing
```javascript
// Open browser console and test
const ws = new WebSocket('ws://<loadbalancer-ip>:8081/ws-notifications');
ws.onopen = () => console.log('WebSocket connected');
ws.onmessage = (event) => console.log('Message:', event.data);
ws.onerror = (error) => console.error('Error:', error);
```

### Command Line Testing
```bash
# Install wscat
npm install -g wscat

# Test WebSocket
wscat -c ws://<loadbalancer-ip>:8081/ws-notifications
```

### Curl Testing
```bash
# Test WebSocket upgrade
curl -v \
  -H "Upgrade: websocket" \
  -H "Connection: Upgrade" \
  -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
  -H "Sec-WebSocket-Version: 13" \
  http://<loadbalancer-ip>:8081/ws-notifications
```

## Performance Optimizations

### 1. Session Affinity
```yaml
sessionAffinity: ClientIP
sessionAffinityConfig:
  clientIP:
    timeoutSeconds: 3600  # 1 hour
```

### 2. Resource Limits
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

### 3. Health Checks
```yaml
livenessProbe:
  httpGet:
    path: /api/actuator/health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
```

### 4. CORS Configuration
```yaml
env:
- name: SPRING_WEB_CORS_ALLOWED_ORIGINS
  value: "*"  # Allow all origins for WebSocket
```

## Troubleshooting

### LoadBalancer IP Not Assigned
```powershell
# Check service status
kubectl describe service websocket-loadbalancer -n microsponsoring

# Check if LoadBalancer is supported
kubectl get nodes -o wide
```

### WebSocket Connection Failed
```powershell
# Check pod logs
kubectl logs -l app=backend -n microsponsoring --tail=50

# Check service endpoints
kubectl get endpoints -n microsponsoring

# Test direct pod access
kubectl exec -it <pod-name> -n microsponsoring -- wget -qO- http://localhost:8081/ws-notifications
```

### CORS Issues
```powershell
# Check CORS configuration
kubectl get deployment backend-deployment -n microsponsoring -o yaml | grep -i cors

# Update CORS settings
kubectl patch deployment backend-deployment -n microsponsoring -p '{"spec":{"template":{"spec":{"containers":[{"name":"backend","env":[{"name":"SPRING_WEB_CORS_ALLOWED_ORIGINS","value":"*"}]}]}}}}'
```

## Comparison: LoadBalancer vs NodePort + Ingress

| Feature | LoadBalancer | NodePort + Ingress |
|---------|-------------|-------------------|
| **Performance** | ✅ Direct connection | ❌ Double proxy |
| **Latency** | ✅ Lower | ❌ Higher |
| **Configuration** | ✅ Simple | ❌ Complex |
| **WebSocket Support** | ✅ Native | ❌ Needs special config |
| **Session Affinity** | ✅ Built-in | ❌ Complex setup |
| **Production Ready** | ✅ Yes | ❌ Limited |
| **Cost** | ❌ Higher | ✅ Lower |
| **Local Development** | ❌ Needs cloud | ✅ Works locally |

## Recommendation

**Use LoadBalancer for WebSocket** because:
1. ✅ Better performance and reliability
2. ✅ Simpler configuration
3. ✅ Native WebSocket support
4. ✅ Production ready
5. ✅ Better session handling

The only downside is cost, but for production WebSocket applications, the benefits far outweigh the costs.
