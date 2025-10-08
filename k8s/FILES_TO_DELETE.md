# Files You Can Delete - Clean Up Guide

## Files You Can SAFELY DELETE (Old/Unnecessary)

### Old Deployment Files:
- `backend-deployment.yaml` (old version)
- `frontend-deployment.yaml` (old version)
- `mysql-deployment.yaml` (old version)
- `image-service.yaml` (old version)

### Ingress Files (No longer needed):
- `ingress.yaml`
- `ingress-simple.yaml`
- `hybrid-loadbalancer-ingress.yaml`
- `websocket-headers.yaml`

### Old Service Files:
- `loadbalancer-services.yaml` (old version)
- `loadbalancer-only-services.yaml` (old version)
- `websocket-service.yaml`
- `websocket-loadbalancer.yaml`

### Old Deployment Scripts:
- `deploy-complete-fix.ps1`
- `deploy-complete-fix.sh`
- `deploy-fixed.ps1`
- `deploy-fixed.sh`
- `deploy-with-loadbalancer.ps1`
- `cleanup-ingress.ps1`

### Old Test Scripts:
- `test-websocket-k8s.sh`
- `test-websocket-loadbalancer.ps1`
- `test-websocket-simple.ps1`
- `test-websocket.sh`

### Documentation Files (Keep if you want reference):
- `DEPLOYMENT_FIX_SUMMARY.md`
- `FIXES_APPLIED.md`
- `INGRESS_CLEANUP_GUIDE.md`
- `LOADBALANCER_OPTIONS.md`
- `WEBSOCKET_KUBERNETES_FIX.md`
- `WEBSOCKET_LOADBALANCER_GUIDE.md`
- `WEBSOCKET_SOLUTION.md`

## Files You SHOULD KEEP (Essential)

### Core Deployment:
- `FINAL_CLEAN_DEPLOYMENT.yaml` ⭐ **MAIN FILE**
- `cleanup-and-deploy-final.ps1` ⭐ **MAIN SCRIPT**

### Supporting Files:
- `namespace.yaml` (if you want to keep it separate)
- `secrets.yaml` (if you want to keep it separate)
- `storage-class.yaml` (if you want to keep it separate)
- `monitoring.yaml` (if you want to keep it separate)

## Quick Cleanup Commands

### Delete Old Files (Windows):
```powershell
# Delete old deployment files
Remove-Item backend-deployment.yaml, frontend-deployment.yaml, mysql-deployment.yaml, image-service.yaml

# Delete ingress files
Remove-Item ingress.yaml, ingress-simple.yaml, hybrid-loadbalancer-ingress.yaml, websocket-headers.yaml

# Delete old service files
Remove-Item loadbalancer-services.yaml, loadbalancer-only-services.yaml, websocket-service.yaml, websocket-loadbalancer.yaml

# Delete old scripts
Remove-Item deploy-complete-fix.ps1, deploy-complete-fix.sh, deploy-fixed.ps1, deploy-fixed.sh, deploy-with-loadbalancer.ps1, cleanup-ingress.ps1

# Delete old test scripts
Remove-Item test-websocket-k8s.sh, test-websocket-loadbalancer.ps1, test-websocket-simple.ps1, test-websocket.sh
```

### Delete Old Files (Linux):
```bash
# Delete old deployment files
rm backend-deployment.yaml frontend-deployment.yaml mysql-deployment.yaml image-service.yaml

# Delete ingress files
rm ingress.yaml ingress-simple.yaml hybrid-loadbalancer-ingress.yaml websocket-headers.yaml

# Delete old service files
rm loadbalancer-services.yaml loadbalancer-only-services.yaml websocket-service.yaml websocket-loadbalancer.yaml

# Delete old scripts
rm deploy-complete-fix.ps1 deploy-complete-fix.sh deploy-fixed.ps1 deploy-fixed.sh deploy-with-loadbalancer.ps1 cleanup-ingress.ps1

# Delete old test scripts
rm test-websocket-k8s.sh test-websocket-loadbalancer.ps1 test-websocket-simple.ps1 test-websocket.sh
```

## Final Clean Directory Structure

After cleanup, you should have:
```
k8s/
├── FINAL_CLEAN_DEPLOYMENT.yaml    ⭐ Main deployment file
├── cleanup-and-deploy-final.ps1   ⭐ Main deployment script
├── namespace.yaml                 (optional)
├── secrets.yaml                   (optional)
├── storage-class.yaml             (optional)
├── monitoring.yaml                (optional)
└── README.md                      (optional)
```

## What the Final Deployment Includes

### ✅ **All Essential Resources:**
- MySQL database (1 replica)
- Backend API (2 replicas) with LoadBalancer
- Frontend (1 replica) with LoadBalancer
- Image service (1 replica) with LoadBalancer
- Internal services for communication
- Proper resource limits and health checks
- WebSocket support with session affinity

### ✅ **Clean Architecture:**
- No Ingress complexity
- Direct LoadBalancer access
- Optimized for WebSocket
- Production-ready configuration

### ✅ **Single File Deployment:**
- Everything in one YAML file
- Easy to manage and version control
- No complex dependencies

## Recommendation

**Delete all the old files** and use only:
1. `FINAL_CLEAN_DEPLOYMENT.yaml` - Your main deployment
2. `cleanup-and-deploy-final.ps1` - Your deployment script

This gives you a clean, simple, and maintainable setup!
