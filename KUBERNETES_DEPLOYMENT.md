# Kubernetes Deployment Guide for Microsponsoring Backend

## Prerequisites

1. **Kubernetes cluster** running and accessible
2. **kubectl** configured to connect to your cluster
3. **Docker** images built and pushed to a registry (or available locally)

## Quick Deployment

### Option 1: Using the deployment script (Recommended)

**On Windows (PowerShell):**
```powershell
.\deploy-to-k8s.ps1
```

**On Linux/Unix:**
```bash
./deploy-to-k8s.sh
```

### Option 2: Manual deployment

1. **Create namespace:**
   ```bash
   kubectl apply -f k8s/namespace.yaml
   ```

2. **Create configuration:**
   ```bash
   kubectl apply -f k8s/configmap.yaml
   kubectl apply -f k8s/secret.yaml
   ```

3. **Deploy database:**
   ```bash
   kubectl apply -f k8s/postgres.yaml
   kubectl wait --for=condition=ready pod -l app=postgres -n microsponsoring --timeout=300s
   ```

4. **Deploy cache:**
   ```bash
   kubectl apply -f k8s/redis.yaml
   kubectl wait --for=condition=ready pod -l app=redis -n microsponsoring --timeout=300s
   ```

5. **Deploy application:**
   ```bash
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   kubectl apply -f k8s/ingress.yaml
   ```

6. **Wait for deployment:**
   ```bash
   kubectl wait --for=condition=available deployment/microsponsoring-backend -n microsponsoring --timeout=300s
   ```

## Verification

Check the deployment status:
```bash
kubectl get pods -n microsponsoring
kubectl get services -n microsponsoring
kubectl get ingress -n microsponsoring
```

## Monitoring

View application logs:
```bash
kubectl logs -f deployment/microsponsoring-backend -n microsponsoring
```

## Troubleshooting

### Check pod status:
```bash
kubectl describe pod <pod-name> -n microsponsoring
```

### Check service endpoints:
```bash
kubectl get endpoints -n microsponsoring
```

### Check ingress status:
```bash
kubectl describe ingress microsponsoring-ingress -n microsponsoring
```

## Scaling

Scale the application:
```bash
kubectl scale deployment microsponsoring-backend --replicas=3 -n microsponsoring
```

## Rolling Update

Update the application with a new image:
```bash
kubectl set image deployment/microsponsoring-backend microsponsoring-backend=your-registry/microsponsoring-backend:latest -n microsponsoring
```

## Cleanup

To remove all resources:
```bash
kubectl delete namespace microsponsoring
```

## Configuration

### Environment Variables

The application uses the following environment variables (configured in `k8s/configmap.yaml` and `k8s/secret.yaml`):

- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_REDIS_HOST`: Redis host
- `SPRING_REDIS_PORT`: Redis port
- `SPRING_REDIS_PASSWORD`: Redis password
- `JWT_SECRET`: JWT secret key
- `SERVER_PORT`: Application port (default: 8080)

### Resource Limits

The deployment includes resource limits and requests:
- CPU: 500m request, 1000m limit
- Memory: 512Mi request, 1Gi limit

## Security

- All sensitive data is stored in Kubernetes secrets
- The application runs as a non-root user
- Network policies can be applied for additional security

## Backup

For production deployments, ensure you have:
1. Database backups configured
2. Persistent volume backups
3. Configuration backups
4. Monitoring and alerting set up
