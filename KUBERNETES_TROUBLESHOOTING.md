# Kubernetes Troubleshooting Guide

## Issues Fixed

### 1. WebSocket Connection Issues

**Problem**: `WebSocket connection to 'wss://192.168.179.128/ws-notifications' failed: Error in connection establishment: net::ERR_CONNECTION_REFUSED`

**Root Cause**: The frontend was trying to connect to a hardcoded IP address that's not accessible from within the Kubernetes cluster.

**Solution**: 
- Updated `environment.pod.ts` to use the correct WebSocket URL: `ws://microsponsoring.local/ws-notifications`
- Created an Ingress configuration to properly route WebSocket traffic
- Fixed the frontend deployment configuration

### 2. Image Loading Issues

**Problem**: `GET http://api/images/... net::ERR_NAME_NOT_RESOLVED`

**Root Cause**: The frontend was trying to load images with incorrect API paths.

**Solution**:
- The API paths are now correctly configured to use relative paths (`/api/images/...`)
- The Ingress configuration routes `/api` requests to the backend service
- Backend has proper image serving configuration

### 3. Frontend Deployment Issues

**Problem**: The `frontend-deployment.yaml` was incorrectly configured (deploying backend instead of frontend).

**Solution**:
- Fixed the deployment to properly deploy the frontend container
- Updated service configuration
- Removed unnecessary volume mounts

## Deployment Steps

### 1. Prerequisites

Make sure you have:
- Kubernetes cluster running
- kubectl configured
- Docker images built and pushed to registry

### 2. Deploy the Application

**For Windows (PowerShell):**
```powershell
.\deploy-to-k8s-fixed.ps1
```

**For Linux/Mac:**
```bash
./deploy-to-k8s-fixed.sh
```

### 3. Access the Application

#### Option A: Using Ingress (Recommended)
1. Add to your hosts file:
   - Windows: `C:\Windows\System32\drivers\etc\hosts`
   - Linux/Mac: `/etc/hosts`
   
   Add this line:
   ```
   127.0.0.1 microsponsoring.local
   ```

2. Access the application:
   - Frontend: http://microsponsoring.local
   - Backend API: http://microsponsoring.local/api
   - WebSocket: ws://microsponsoring.local/ws-notifications

#### Option B: Using Port Forwarding
If Ingress is not available:

```bash
# Frontend
kubectl port-forward -n microsponsoring svc/frontend-service 8080:80

# Backend
kubectl port-forward -n microsponsoring svc/backend-service 8081:80
```

Then access:
- Frontend: http://localhost:8080
- Backend: http://localhost:8081

## Troubleshooting Commands

### Check Pod Status
```bash
kubectl get pods -n microsponsoring
kubectl describe pod <pod-name> -n microsponsoring
```

### Check Service Status
```bash
kubectl get services -n microsponsoring
kubectl describe service <service-name> -n microsponsoring
```

### Check Logs
```bash
# Backend logs
kubectl logs -n microsponsoring -l app=backend --tail=50

# Frontend logs
kubectl logs -n microsponsoring -l app=frontend --tail=50

# MySQL logs
kubectl logs -n microsponsoring -l app=mysql --tail=50
```

### Check Ingress
```bash
kubectl get ingress -n microsponsoring
kubectl describe ingress microsponsoring-ingress -n microsponsoring
```

### Test WebSocket Connection
```bash
# Test from within the cluster
kubectl exec -it <frontend-pod> -n microsponsoring -- wget -O- http://backend-service.microsponsoring.svc.cluster.local:80/ws-notifications
```

## Common Issues and Solutions

### 1. WebSocket Still Not Working

**Check**: Is the Ingress controller properly configured for WebSocket support?

**Solution**: Make sure your Ingress controller (nginx, traefik, etc.) supports WebSocket upgrades.

### 2. Images Not Loading

**Check**: Are the image files actually present in the backend pod?

**Solution**: 
```bash
kubectl exec -it <backend-pod> -n microsponsoring -- ls -la /app/images/
```

### 3. Database Connection Issues

**Check**: Is MySQL running and accessible?

**Solution**:
```bash
kubectl exec -it <mysql-pod> -n microsponsoring -- mysql -u root -e "SHOW DATABASES;"
```

### 4. Frontend Not Loading

**Check**: Is the frontend container running and serving files?

**Solution**:
```bash
kubectl exec -it <frontend-pod> -n microsponsoring -- ls -la /usr/share/nginx/html/
```

## Environment Configuration

The application uses different environment configurations:

- **Local Development**: `environment.ts` - uses localhost URLs
- **Kubernetes Production**: `environment.pod.ts` - uses service names and ingress

Make sure you're building with the correct configuration:
```bash
# For Kubernetes deployment
npm run build -- --configuration pod
```

## Monitoring

### Health Checks
- Backend: http://microsponsoring.local/api/health/health
- Frontend: http://microsponsoring.local/

### Metrics
- Prometheus: http://microsponsoring.local:9090 (if monitoring is deployed)
- Grafana: http://microsponsoring.local:3000 (if monitoring is deployed)

## Cleanup

To remove the deployment:
```bash
kubectl delete namespace microsponsoring
```

## Support

If you continue to have issues:

1. Check the logs for specific error messages
2. Verify all services are running and healthy
3. Test connectivity between services
4. Check the Ingress controller configuration
5. Verify the environment configuration matches your setup
