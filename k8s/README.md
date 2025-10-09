# Microsponsoring Kubernetes Deployment

## Clean, Simple Deployment

This folder now contains only the essential files needed for your microsponsoring application.

## Files Overview

### 🚀 **Main Deployment Files**
- **`FINAL_CLEAN_DEPLOYMENT.yaml`** - Complete deployment with all services
  - MySQL database
  - Backend API with WebSocket support
  - Frontend application
  - Image service
  - LoadBalancer services for external access

- **`cleanup-and-deploy-final.ps1`** - PowerShell script to deploy everything
  - Cleans up old resources
  - Deploys the final clean version
  - Shows access information

### 🔧 **Optional Configuration Files**
- **`namespace.yaml`** - Namespace definition (optional, included in main file)
- **`secrets.yaml`** - Secrets configuration (optional, included in main file)
- **`storage-class.yaml`** - Storage class for persistent volumes (optional)
- **`monitoring.yaml`** - Monitoring configuration (optional)

## Quick Start

### 1. Deploy Everything
```powershell
.\cleanup-and-deploy-final.ps1
```

### 2. Check Status
```powershell
kubectl get pods -n microsponsoring
kubectl get services -n microsponsoring
```

### 3. Access Your Application
After deployment, you'll get LoadBalancer IPs for:
- **Frontend**: `http://<frontend-ip>`
- **API**: `http://<backend-ip>:8080`
- **WebSocket**: `ws://<backend-ip>:8081`
- **Images**: `http://<image-ip>`

## What's Included

### ✅ **Services**
- MySQL database (1 replica)
- Backend API (2 replicas) with LoadBalancer
- Frontend (1 replica) with LoadBalancer
- Image service (1 replica) with LoadBalancer

### ✅ **Features**
- WebSocket support with session affinity
- Image upload and serving
- Database persistence
- Health checks and resource limits
- CORS configuration
- Security settings

### ✅ **Architecture**
- LoadBalancer-only (no Ingress complexity)
- Direct service access
- Optimized for WebSocket
- Production-ready configuration

## Cleanup

This folder was cleaned from 35+ files down to just 7 essential files:
- ❌ Removed all old deployment files
- ❌ Removed all Ingress configurations
- ❌ Removed all test scripts
- ❌ Removed all documentation files
- ❌ Removed all duplicate services

## Maintenance

- **To update**: Edit `FINAL_CLEAN_DEPLOYMENT.yaml` and redeploy
- **To scale**: Modify replicas in the deployment file
- **To debug**: Use `kubectl logs` and `kubectl describe` commands

## Support

If you need to add features or modify the deployment:
1. Edit `FINAL_CLEAN_DEPLOYMENT.yaml`
2. Run `.\cleanup-and-deploy-final.ps1`
3. Test your changes

This gives you a clean, maintainable Kubernetes deployment! 🎉