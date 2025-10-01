# Kubernetes Deployment Guide

## Quick Deployment Commands

### Option 1: Using the Deployment Scripts

#### For Linux/Mac:
```bash
# Make the script executable
chmod +x deploy-latest.sh

# Run the deployment
./deploy-latest.sh
```

#### For Windows PowerShell:
```powershell
# Run the deployment
.\deploy-latest.ps1
```

### Option 2: Manual Deployment Commands

```bash
# 1. Apply namespace
kubectl apply -f k8s/namespace.yaml

# 2. Apply secrets (if you have them)
kubectl apply -f k8s/secrets.yaml

# 3. Deploy backend
kubectl apply -f k8s/backend-deployment.yaml

# 4. Deploy frontend
kubectl apply -f k8s/frontend-deployment.yaml

# 5. Apply monitoring (if you have it)
kubectl apply -f k8s/monitoring.yaml
```

## Force Pull Latest Images

To ensure you get the latest Docker images:

```bash
# Force pull latest images for backend
kubectl patch deployment backend-deployment -n microsponsoring -p '{"spec":{"template":{"spec":{"containers":[{"name":"backend","image":"medbaha/pfebackend:lastVer","imagePullPolicy":"Always"}]}}}}'

# Force pull latest images for frontend
kubectl patch deployment frontend-deployment -n microsponsoring -p '{"spec":{"template":{"spec":{"containers":[{"name":"frontend","image":"medbaha/pfefrontend:lastVer","imagePullPolicy":"Always"}]}}}}'

# Wait for rollouts to complete
kubectl rollout status deployment/backend-deployment -n microsponsoring
kubectl rollout status deployment/frontend-deployment -n microsponsoring
```

## Check Deployment Status

```bash
# Check pods
kubectl get pods -n microsponsoring

# Check services
kubectl get services -n microsponsoring

# Check deployments
kubectl get deployments -n microsponsoring

# Check ingress
kubectl get ingress -n microsponsoring
```

## View Logs

```bash
# Backend logs
kubectl logs -f deployment/backend-deployment -n microsponsoring

# Frontend logs
kubectl logs -f deployment/frontend-deployment -n microsponsoring

# Specific pod logs
kubectl logs -f <pod-name> -n microsponsoring
```

## Troubleshooting

### If deployment fails:
```bash
# Check pod status
kubectl describe pods -n microsponsoring

# Check events
kubectl get events -n microsponsoring --sort-by='.lastTimestamp'

# Check resource usage
kubectl top pods -n microsponsoring
```

### If images don't update:
```bash
# Delete pods to force recreation
kubectl delete pods -l app=backend -n microsponsoring
kubectl delete pods -l app=frontend -n microsponsoring
```

## Current Configuration

- **Namespace**: `microsponsoring`
- **Backend Image**: `medbaha/pfebackend:lastVer`
- **Frontend Image**: `medbaha/pfefrontend:lastVer`
- **Replicas**: 3 for each service
- **Backend Port**: 8080
- **Frontend Port**: 80

## Notes

- The deployment uses `imagePullPolicy: Always` to ensure latest images are pulled
- Persistent volumes are configured for images and invoices storage
- Health checks are configured for both services
- Resource limits are set for both services
