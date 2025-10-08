# Ingress Cleanup Guide

## Why Remove Ingress?

### Current Setup (Complex):
```
Client → Ingress Controller → NodePort → Backend
       ↑ Extra layer, complex routing, timeouts
```

### LoadBalancer-Only Setup (Simple):
```
Client → LoadBalancer → Backend
       ↑ Direct connection, better performance
```

## Benefits of Removing Ingress

### ✅ **Simplified Architecture**
- No Ingress Controller needed
- No complex routing rules
- Fewer moving parts to maintain

### ✅ **Better Performance**
- Direct connection to services
- No proxy overhead
- Lower latency

### ✅ **Easier Configuration**
- No rewrite rules needed
- No special annotations
- Simpler debugging

### ✅ **Better WebSocket Support**
- Direct WebSocket connections
- No proxy timeouts
- Better session handling

### ✅ **Cost Savings**
- No Ingress Controller resources
- Simpler infrastructure

## What Gets Removed

### Ingress Resources:
- `microsponsoring-ingress`
- `microsponsoring-ingress-simple`
- `microsponsoring-ingress-fixed`
- `websocket-headers` ConfigMap

### NodePort Services:
- `backend-nodeport`
- `frontend-nodeport`
- `websocket-nodeport`

### Ingress Controller (Optional):
- `ingress-nginx` namespace
- Ingress Controller pods

## What Stays

### LoadBalancer Services:
- `frontend-loadbalancer`
- `backend-loadbalancer`
- `image-loadbalancer`

### Internal Services:
- `backend-service` (ClusterIP)
- `frontend-service` (ClusterIP)
- `image-service` (ClusterIP)

## Cleanup Steps

### 1. Run Cleanup Script
```powershell
.\cleanup-ingress.ps1
```

### 2. Manual Cleanup (if needed)
```bash
# Remove Ingress resources
kubectl delete ingress microsponsoring-ingress -n microsponsoring
kubectl delete ingress microsponsoring-ingress-simple -n microsponsoring
kubectl delete ingress microsponsoring-ingress-fixed -n microsponsoring

# Remove NodePort services
kubectl delete service backend-nodeport -n microsponsoring
kubectl delete service frontend-nodeport -n microsponsoring
kubectl delete service websocket-nodeport -n microsponsoring

# Remove Ingress-related ConfigMaps
kubectl delete configmap websocket-headers -n microsponsoring

# Apply LoadBalancer-only services
kubectl apply -f loadbalancer-only-services.yaml
```

### 3. Remove Ingress Controller (Optional)
```bash
# Only if not used by other applications
kubectl delete namespace ingress-nginx
```

## New Access Pattern

### Before (with Ingress):
- Frontend: `http://microsponsoring.local:32403`
- API: `http://microsponsoring.local:32403/api`
- WebSocket: `ws://microsponsoring.local:32403/ws-notifications`

### After (LoadBalancer-only):
- Frontend: `http://<frontend-lb-ip>`
- API: `http://<backend-lb-ip>:8080`
- WebSocket: `ws://<backend-lb-ip>:8081`
- Images: `http://<image-lb-ip>`

## Frontend Configuration Update

### Update `environment.pod.ts`:
```typescript
export const environment = {
  production: true,
  // Use LoadBalancer IPs directly
  apiUrl: 'http://<backend-lb-ip>:8080/api',
  wsUrl: 'ws://<backend-lb-ip>:8081/ws-notifications',
  imageUrl: 'http://<image-lb-ip>',
  baseUrl: 'http://<frontend-lb-ip>',
  
  // Or use environment variables
  apiUrl: process.env['API_URL'] || 'http://<backend-lb-ip>:8080/api',
  wsUrl: process.env['WS_URL'] || 'ws://<backend-lb-ip>:8081/ws-notifications',
  imageUrl: process.env['IMAGE_URL'] || 'http://<image-lb-ip>',
  baseUrl: process.env['BASE_URL'] || 'http://<frontend-lb-ip>'
};
```

## Testing After Cleanup

### 1. Check Service Status
```bash
kubectl get services -n microsponsoring
```

### 2. Test Direct Access
```bash
# Test API
curl http://<backend-lb-ip>:8080/api/actuator/health

# Test WebSocket
curl -v \
  -H "Upgrade: websocket" \
  -H "Connection: Upgrade" \
  -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
  -H "Sec-WebSocket-Version: 13" \
  http://<backend-lb-ip>:8081/ws-notifications
```

### 3. Test Frontend
```bash
# Test frontend
curl http://<frontend-lb-ip>
```

## Troubleshooting

### LoadBalancer IP Not Assigned
```bash
# Check service status
kubectl describe service <service-name> -n microsponsoring

# Check if LoadBalancer is supported
kubectl get nodes -o wide
```

### Services Not Accessible
```bash
# Check pod status
kubectl get pods -n microsponsoring

# Check service endpoints
kubectl get endpoints -n microsponsoring

# Check pod logs
kubectl logs -l app=backend -n microsponsoring --tail=50
```

### WebSocket Issues
```bash
# Test WebSocket directly on pod
kubectl exec -it <pod-name> -n microsponsoring -- wget -qO- http://localhost:8081/ws-notifications

# Check CORS configuration
kubectl get deployment backend-deployment -n microsponsoring -o yaml | grep -i cors
```

## Rollback Plan

If you need to rollback to Ingress:

### 1. Restore Ingress Resources
```bash
kubectl apply -f ingress.yaml
kubectl apply -f websocket-headers.yaml
```

### 2. Restore NodePort Services
```bash
kubectl apply -f frontend-nodeport.yaml
kubectl apply -f backend-nodeport.yaml
```

### 3. Update Frontend Configuration
```typescript
// Revert to Ingress URLs
apiUrl: 'http://microsponsoring.local:32403/api',
wsUrl: 'ws://microsponsoring.local:32403/ws-notifications',
```

## Summary

Removing Ingress simplifies your architecture and improves performance, especially for WebSocket connections. The LoadBalancer-only approach is:

- ✅ **Simpler** - Fewer components to manage
- ✅ **Faster** - Direct connections
- ✅ **More Reliable** - Fewer failure points
- ✅ **Better for WebSocket** - No proxy issues
- ✅ **Cost Effective** - Fewer resources needed

**Recommendation**: Remove Ingress and use LoadBalancer-only setup for better performance and simplicity.
