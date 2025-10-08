# LoadBalancer Options for Microsponsoring

## Option 1: Separate LoadBalancers (Recommended for Production)

### Benefits:
- ✅ Each service gets its own external IP
- ✅ Better isolation and security
- ✅ Easier to scale individual services
- ✅ More granular control

### Usage:
```powershell
kubectl apply -f loadbalancer-services.yaml
```

### Access:
- Frontend: `http://<frontend-ip>`
- API: `http://<backend-ip>:8080`
- WebSocket: `ws://<backend-ip>:8081`
- Images: `http://<image-ip>`

## Option 2: Hybrid LoadBalancer + Ingress (Best of Both Worlds)

### Benefits:
- ✅ Single external IP for all services
- ✅ Internal routing through ingress
- ✅ Better for development/testing
- ✅ Easier DNS management

### Usage:
```powershell
kubectl apply -f hybrid-loadbalancer-ingress.yaml
```

### Access:
- All services: `http://<loadbalancer-ip>`
- API: `http://<loadbalancer-ip>/api`
- WebSocket: `ws://<loadbalancer-ip>/ws-notifications`
- Images: `http://<loadbalancer-ip>/images`

## Option 3: Keep Current NodePort Setup

### Benefits:
- ✅ No external dependencies
- ✅ Works in any environment
- ✅ Good for development

### Drawbacks:
- ❌ Requires manual port management
- ❌ Need to modify /etc/hosts
- ❌ Less suitable for production

## Comparison Table

| Feature | Separate LoadBalancers | Hybrid LoadBalancer+Ingress | NodePort |
|---------|----------------------|----------------------------|----------|
| External IPs | Multiple | Single | None |
| DNS Setup | Complex | Simple | Manual |
| Production Ready | ✅ | ✅ | ❌ |
| Development Friendly | ❌ | ✅ | ✅ |
| Cost | Higher | Medium | Low |
| Complexity | High | Medium | Low |

## Recommendations

### For Development/Testing:
Use **Hybrid LoadBalancer + Ingress** - Single IP, easy to manage

### For Production:
Use **Separate LoadBalancers** - Better isolation and security

### For Local Development:
Keep **NodePort** - No external dependencies

## Implementation Steps

### 1. Deploy with LoadBalancer:
```powershell
.\deploy-with-loadbalancer.ps1
```

### 2. Check External IPs:
```powershell
kubectl get services -n microsponsoring
```

### 3. Update Frontend Configuration:
Update `environment.pod.ts` with the LoadBalancer IPs:
```typescript
export const environment = {
  production: true,
  apiUrl: 'http://<backend-ip>:8080/api',
  wsUrl: 'ws://<backend-ip>:8081/ws-notifications',
  imageUrl: 'http://<image-ip>',
  baseUrl: 'http://<frontend-ip>'
};
```

### 4. Test Connectivity:
```powershell
.\test-websocket-simple.ps1
```

## Troubleshooting

### LoadBalancer IP Not Assigned:
- Check if your cluster supports LoadBalancer (cloud provider required)
- For local clusters, consider using MetalLB or similar
- Fall back to NodePort if LoadBalancer not available

### Services Not Accessible:
- Check if pods are running: `kubectl get pods -n microsponsoring`
- Check service endpoints: `kubectl get endpoints -n microsponsoring`
- Check LoadBalancer status: `kubectl describe service <service-name> -n microsponsoring`

### WebSocket Issues:
- Ensure WebSocket port is properly exposed
- Check firewall rules for WebSocket ports
- Verify CORS configuration
